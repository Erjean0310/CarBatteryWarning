package com.erjean.carbatterywarning.job;

import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 定时任务扫描电池信号数据
 */
@Component
@Slf4j
public class WarnProducer {
    @Resource
    private BatterySignalMapper batterySignalMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedDelay = 1000 * 10)
    public void run() {
        log.info("开始处理告警");

        // 查询近 5 分钟内的数据
        Date date = new Date(new Date().getTime() - 5 * 60 * 1000);
        List<BatterySignal> batterySignalList = batterySignalMapper.selectSignalsByDate(date);
        if (batterySignalList != null && !batterySignalList.isEmpty()) {
            for (BatterySignal batterySignal : batterySignalList) {
                System.out.println("开始发送数据");
                rocketMQTemplate.convertAndSend("warn-topic", batterySignal);
                System.out.println("发送了数据:" + batterySignal);
            }

//            // 处理告警
//            System.out.println("告警数据为: " + batterySignalList);
        }
    }
}
