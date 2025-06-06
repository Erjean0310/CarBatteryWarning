package com.erjean.carbatterywarning.job;

import com.erjean.carbatterywarning.constant.RedisConstants;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.utils.RedisUtils;
import com.erjean.carbatterywarning.utils.RocketMqUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 定时任务扫描电池信号数据
 */
@Component
@Slf4j
public class WarnProducer {
    @Resource
    private BatterySignalMapper batterySignalMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private RocketMqUtils rocketMqUtils;

    @Resource
    private ObjectMapper cleanObjectMapper;

    private static final int BATCH_SIZE = 100;
    private static final int THREAD_COUNT = 4;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

    // 分布式锁的键和超时时间
    private static final String TASK_LOCK_KEY = "warn_producer_task_lock";
    private static final long LOCK_EXPIRE_TIME = 60;

    /**
     * 获取分布式锁
     */
    private boolean acquireLock() {
        return redisUtils.setNx(TASK_LOCK_KEY, "locked", LOCK_EXPIRE_TIME);
    }

    /**
     * 释放分布式锁
     */
    private void releaseLock() {
        redisUtils.delete(TASK_LOCK_KEY);
    }

    /**
     * 定时任务，每分钟执行一次，扫描 Redis 存储的 id 列表
     */
    @Scheduled(fixedDelay = 1000 * 60)
    public void run() {
        if (!acquireLock()) {
            return;
        }
        try {
            log.info("开始处理信号");

            // 获取当前队列大小
            Long signalCount = redisUtils.listSize(RedisConstants.SIGNAL_PENDING_QUEUE_KEY);
            if (signalCount == null || signalCount <= 0) {
                log.info("暂无待处理信号");
                return;
            }

            List<Object> signalObjList = redisUtils.listRange(
                    RedisConstants.SIGNAL_PENDING_QUEUE_KEY, 0, signalCount - 1);

            if (signalObjList == null || signalObjList.isEmpty()) {
                return;
            }

            int realSignalCount = signalObjList.size();

            // 转换为 Long 类型的 ID 列表
            List<Long> ids = signalObjList.stream()
                    .map(obj -> ((Number) obj).longValue())
                    .collect(Collectors.toList());

            // 分批处理
            int batchCount = (int) Math.ceil((double) realSignalCount / BATCH_SIZE);
            CountDownLatch latch = new CountDownLatch(batchCount);

            for (int i = 0; i < batchCount; i++) {
                int start = i * BATCH_SIZE;
                int end = Math.min(start + BATCH_SIZE, realSignalCount);
                List<Long> subList = ids.subList(start, end);

                executorService.submit(() -> {
                    try {
                        List<BatterySignal> batterySignalList = batterySignalMapper.selectByIds(subList);
                        if (batterySignalList != null && !batterySignalList.isEmpty()) {
                            // 将 BatterySignal 列表转换为 Message 列表
                            List<Message> messagesToSend = batterySignalList.stream()
                                    .map(signal -> {
                                        try {
                                            // 使用 ObjectMapper 将 BatterySignal 对象序列化为 JSON 字符串作为消息体
                                            byte[] body = cleanObjectMapper.writeValueAsBytes(signal);
                                            return new Message("warn-topic", body);
                                        } catch (Exception e) {
                                            log.error("序列化 BatterySignal 失败", e);
                                            return null;
                                        }
                                    })
                                    .filter(java.util.Objects::nonNull)
                                    .collect(Collectors.toList());

                            if (!messagesToSend.isEmpty()) {
                                rocketMqUtils.batchSendMessage("warn-topic", messagesToSend);
                                System.out.println("批量发送了 " + messagesToSend.size() + " 条数据到 warn-topic");
                            }
                        }
                    } catch (Exception e) {
                        log.error("处理电池信号异常", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待线程完成时发生中断异常", e);
            }

            redisUtils.listTrim(RedisConstants.SIGNAL_PENDING_QUEUE_KEY, realSignalCount, -1);
        } finally {
            releaseLock(); // 释放分布式锁
        }
    }

    /**
     * 定时任务，每 5 分钟执行一次，扫描数据库中近 15分钟内的数据
     */
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void run2() {
        if (!acquireLock()) {
            return;
        }

        try {
            log.info("开始处理告警");

            // 查询近 15 分钟内的数据
            Date date = new Date(new Date().getTime() - 15 * 60 * 1000);
            List<BatterySignal> batterySignalList = batterySignalMapper.selectSignalsByDate(date);

            if (batterySignalList == null || batterySignalList.isEmpty()) {
                log.info("暂无新的电池信号数据");
                return;
            }

            int realSignalCount = batterySignalList.size();

            // 分批处理
            int batchCount = (int) Math.ceil((double) realSignalCount / BATCH_SIZE);
            CountDownLatch latch = new CountDownLatch(batchCount);

            for (int i = 0; i < batchCount; i++) {
                int start = i * BATCH_SIZE;
                int end = Math.min(start + BATCH_SIZE, realSignalCount);
                List<BatterySignal> subList = batterySignalList.subList(start, end);

                executorService.submit(() -> {
                    try {
                        // 将 BatterySignal 列表转换为 Message 列表
                        List<Message> messagesToSend = subList.stream()
                                .map(signal -> {
                                    try {
                                        byte[] body = cleanObjectMapper.writeValueAsBytes(signal);
                                        return new Message("warn-topic", body);
                                    } catch (Exception e) {
                                        log.error("序列化 BatterySignal 失败", e);
                                        return null;
                                    }
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList());

                        if (!messagesToSend.isEmpty()) {
                            rocketMqUtils.batchSendMessage("warn-topic", messagesToSend);
                            log.info("批量发送了 {} 条数据到 warn-topic", messagesToSend.size());
                        }
                    } catch (Exception e) {
                        log.error("处理电池信号异常", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待线程完成时发生中断异常", e);
            }
        } finally {
            releaseLock();
        }
    }
}
