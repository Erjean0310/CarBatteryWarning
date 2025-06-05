package com.erjean.carbatterywarning;

import com.erjean.carbatterywarning.model.domain.Rule;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

@SpringBootTest
class CarBatteryWarningApplicationTests {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testJsonToObject() {
        String json = "[{\"max\": null, \"min\": 1.0, \"level\": 0}, {\"max\": 1.0, \"min\": 0.5, \"level\": 1}, {\"max\": 0.5, \"min\": 0.2, \"level\": 2}]";

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 将 JSON 字符串直接反序列化为 Rule 数组
            Rule[] ruleArray = objectMapper.readValue(json, Rule[].class);

            // 打印结果验证
            for (Rule rule : ruleArray) {
                System.out.println("min: " + rule.getMin() + ", max: " + rule.getMax() + ", level: " + rule.getLevel());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    void mqTest() {
//        rocketMQTemplate.convertAndSend("warn-topic", "你好0605");
        BatterySignal batterySignal = new BatterySignal();
        batterySignal.setId(11L);
        batterySignal.setVid("123");
        batterySignal.setCarId(1L);
        batterySignal.setBatteryType(0);
        batterySignal.setSignal(null);
        batterySignal.setReportTime(new Date());
        batterySignal.setProcessed(0);
        batterySignal.setProcessedTime(null);
        rocketMQTemplate.convertAndSend("warn-topic", batterySignal);
    }

    @Test
    void mqTest2() {
        rocketMQTemplate.convertAndSend("test052801", "你好0605");
    }
}
