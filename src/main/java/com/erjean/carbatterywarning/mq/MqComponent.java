package com.erjean.carbatterywarning.mq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class MqComponent {

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.name-server}")
    private String nameServer;

    private DefaultMQProducer producer;

    // 假设使用RocketMQTemplate进行发送，如果需要更底层控制，可以使用DefaultMQProducer
    // @Autowired
    // private RocketMQTemplate rocketMQTemplate;

    @PostConstruct
    public void init() throws Exception {
        // 初始化生产者，保持连接
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.start();
        System.out.println("RocketMQ Producer Started.");
    }

    @PreDestroy
    public void destroy() {
        // 关闭生产者连接
        if (producer != null) {
            producer.shutdown();
            System.out.println("RocketMQ Producer Shutdown.");
        }
    }

    /**
     * 批量发送消息
     * @param topic 消息主题
     * @param messages 要发送的消息列表
     */
    public void batchSendMessage(String topic, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        MessageListSplitter splitter = new MessageListSplitter(messages);
        while (splitter.hasNext()) {
            try {
                List<Message> subList = splitter.next();
                // 使用生产者发送批量消息
                producer.send(subList);
                System.out.println("发送消息数量"+subList.size());
                System.out.println("Sent batch messages to topic: " + topic);
            } catch (Exception e) {
                System.err.println("Failed to send batch messages: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 批量消费消息的示例（这通常在消费者端实现，这里只是一个方法签名示例）
     * 在实际应用中，批量消费通常通过配置RocketMQ监听器并设置consumeMode为BATCH来实现。
     * @param messages 接收到的批量消息列表
     */
    public void batchConsumeMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        // 在消费者端，MessageListSplitter通常用于处理接收到的批量消息，
        // 如果需要进一步分割（例如，如果接收到的批量消息超过了某个处理阈值）
        // MessageListSplitter splitter = new MessageListSplitter(messages);
        // while (splitter.hasNext()) {
        //     List<Message> subList = splitter.next();
        //     // 处理分割后的消息子列表
        //     processMessages(subList);
        // }

        // 示例：直接处理接收到的批量消息
        processMessages(messages);
    }

    private void processMessages(List<Message> messages) {
        for (Message message : messages) {
            System.out.println("Received message: " + new String(message.getBody()));
            // 在这里添加实际的消息处理逻辑
        }
    }
}
