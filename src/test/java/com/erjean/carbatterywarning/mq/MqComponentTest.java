package com.erjean.carbatterywarning.mq;

import org.apache.rocketmq.common.message.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class MqComponentTest {

    @Autowired
    private MqComponent mqComponent;

    @Test
    void testBatchSendMessage() {
        String topic = "test_topic"; // 替换为你的测试主题
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 20; i++) { // 创建20条测试消息
            messages.add(new Message(topic, ("Test Message " + i).getBytes()));
        }

        try {
            mqComponent.batchSendMessage(topic, messages);
            System.out.println("Batch send test completed.");
        } catch (Exception e) {
            System.err.println("Batch send test failed: " + e.getMessage());
            e.printStackTrace();
            // 可以添加断言来验证测试失败
            // fail("Batch send test failed: " + e.getMessage());
        }
    }
}
