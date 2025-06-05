package com.erjean.carbatterywarning;

import com.erjean.carbatterywarning.model.domain.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class CarBatteryWarningApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
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
}
