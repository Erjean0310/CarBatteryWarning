package com.erjean.carbatterywarning.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class RocketMqUtils {

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.consumer.topic}")
    private String consumerTopic;

    private DefaultMQProducer producer;
    private DefaultMQPushConsumer consumer;
    private final AtomicBoolean consumerStarted = new AtomicBoolean(false);


    @PostConstruct
    public void init() throws Exception {
        // 初始化生产者
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.start();
    }

    /**
     * 批量发送消息
     *
     * @param topic    消息主题
     * @param messages 要发送的消息列表
     */
    public void batchSendMessage(String topic, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            log.warn("没有要发送的消息");
            return;
        }

        MessageListSplitter splitter = new MessageListSplitter(messages);
        while (splitter.hasNext()) {
            try {
                List<Message> subList = splitter.next();
                producer.send(subList);
                log.info("发送到 Topic: {}, 数量: {}", topic, subList.size());
            } catch (Exception e) {
                log.error("批量发送消息失败", e);
            }
        }
    }


    /**
     * 注册并启动消费者
     *
     * @param listener 消息监听器
     */
    public void registerMessageListener(MessageListenerConcurrently listener) throws Exception {
        if (consumerStarted.get()) {
            return;
        }

        consumer = new DefaultMQPushConsumer("warn-cg");
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe(consumerTopic, "*");

        // 设置批量拉取参数
        consumer.setPullBatchSize(64);
        consumer.setConsumeMessageBatchMaxSize(64);
        consumer.setConsumeThreadMin(10);
        consumer.setConsumeThreadMax(30);

        consumer.registerMessageListener(listener);
        consumer.start();
        consumerStarted.set(true);
    }

    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
        }
        if (consumer != null) {
            consumer.shutdown();
        }
    }
}
