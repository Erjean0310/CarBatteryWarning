package com.erjean.carbatterywarning.job;

import com.erjean.carbatterywarning.loader.WarnDataLoader;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.model.domain.*;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.model.enums.BatteryTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
@Configuration
@Slf4j
public class WarnConsumer {

    @Value("${rocketmq.name-server}")
    private String nameServerAddr;

    @Resource
    private BatterySignalMapper batterySignalMapper;

    @Resource
    private WarnDataLoader warnDataLoader;

    @Resource
    private ObjectMapper cleanObjectMapper;

    @PostConstruct
    public void init() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("warn-cg");
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe("warn-topic", "*");

        // 设置并发参数
        consumer.setConsumeThreadMin(10);  // 最小线程数
        consumer.setConsumeThreadMax(30);  // 最大线程数
        consumer.setPullBatchSize(64);     // 每次从 Broker 拉取最多 64 条消息
        consumer.setConsumeMessageBatchMaxSize(1); // 每次提交处理的消息数量（单条处理）

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    BatterySignal batterySignal = cleanObjectMapper.readValue(json, BatterySignal.class);

                    log.info("接收到数据: {}", batterySignal);

                    // 提取字段
                    Long id = batterySignal.getId();
                    String vid = batterySignal.getVid();
                    Long carId = batterySignal.getCarId();
                    Integer batteryType = batterySignal.getBatteryType();
                    String signalStr = (String) batterySignal.getSignal();
                    Signal signal = null;

                    try {
                        signal = cleanObjectMapper.readValue(signalStr, Signal.class);
                    } catch (Exception e) {
                        log.error("无法解析信号数据: {}", signalStr, e);
                        continue;
                    }

                    Date reportTime = batterySignal.getReportTime();

                    Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> warnRules = warnDataLoader.getWarnRules();
                    BatteryTypeEnum batteryTypeEnum = BatteryTypeEnum.fromValue(batteryType);
                    Map<Integer, WarnRuleData> warnRuleDataMap = warnRules.get(batteryTypeEnum);

                    // 处理 Mx - Mi 差值
                    Float mx = signal.getMx();
                    Float mi = signal.getMi();
                    if (mx != null && mi != null) {
                        Float mDiff = mx - mi;
                        WarnHandlerResult warnHandlerResult = judgeWarn(mDiff, warnRuleDataMap.get(1));
                        if (warnHandlerResult != null) {
                            log.info("【警报】vid: {}, 车架编号: {}, 电池类型: {}, 信号上报时间: {}, 报警类型: {}, 报警等级: {}",
                                    vid, carId, batteryTypeEnum.getName(), reportTime,
                                    warnHandlerResult.getWarnName(), warnHandlerResult.getLevel());
                        }
                    }

                    // 处理 Ix - Ii 差值
                    Float ix = signal.getIx();
                    Float ii = signal.getIi();
                    if (ix != null && ii != null) {
                        Float iDiff = ix - ii;
                        WarnHandlerResult warnHandlerResult = judgeWarn(iDiff, warnRuleDataMap.get(2));
                        if (warnHandlerResult != null) {
                            log.info("【警报】vid: {}, 车架编号: {}, 电池类型: {}, 信号上报时间: {}, 报警类型: {}, 报警等级: {}",
                                    vid, carId, batteryTypeEnum.getName(), reportTime,
                                    warnHandlerResult.getWarnName(), warnHandlerResult.getLevel());
                        }
                    }

                    // 更新数据库
                    batterySignalMapper.updateProcessState(id, new Date());

                } catch (Exception e) {
                    log.error("消费消息失败", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        log.info("原生 RocketMQ 批量+并行消费者已启动");
    }

    public WarnHandlerResult judgeWarn(Float diff, WarnRuleData warnRuleData) {
        Integer warnLevel = -1;
        for (Rule rule : warnRuleData.getRules()) {
            Float max = Optional.ofNullable(rule.getMax()).orElse(Float.MAX_VALUE);
            Float min = Optional.ofNullable(rule.getMin()).orElse(Float.MIN_VALUE);
            if (diff >= min && diff < max) {
                warnLevel = rule.getLevel();
                break;
            }
        }
        if (warnLevel != -1) {
            WarnHandlerResult warnHandlerResult = new WarnHandlerResult();
            warnHandlerResult.setLevel(warnLevel);
            warnHandlerResult.setWarnName(warnRuleData.getWarnName());
            return warnHandlerResult;
        }
        return null;
    }
}
