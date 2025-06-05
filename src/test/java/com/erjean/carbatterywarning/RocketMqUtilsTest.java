package com.erjean.carbatterywarning;

import com.erjean.carbatterywarning.utils.RocketMqUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RocketMqUtilsTest {

    @InjectMocks
    private RocketMqUtils rocketMqUtils;

    @Mock
    private DefaultMQProducer producer;

    @Mock
    private DefaultMQPushConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject mock producer and consumer into RocketMqUtils
        ReflectionTestUtils.setField(rocketMqUtils, "producer", producer);
        ReflectionTestUtils.setField(rocketMqUtils, "consumer", consumer);
        // Inject properties
        ReflectionTestUtils.setField(rocketMqUtils, "producerGroup", "testProducerGroup");
        ReflectionTestUtils.setField(rocketMqUtils, "nameServer", "testNameServer");
        ReflectionTestUtils.setField(rocketMqUtils, "consumerTopic", "testConsumerTopic");
    }


    @Test
    void batchSendMessage_success() throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("topic", "tag", "body".getBytes()));
        messages.add(new Message("topic", "tag", "body".getBytes()));

        rocketMqUtils.batchSendMessage("testTopic", messages);

        verify(producer, times(1)).send(anyList());
    }

    @Test
    void batchSendMessage_emptyList() throws Exception {
        List<Message> messages = Collections.emptyList();

        rocketMqUtils.batchSendMessage("testTopic", messages);

        verify(producer, never()).send(anyList());
    }

    @Test
    void batchSendMessage_sendError() throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("topic", "tag", "body".getBytes()));

        doThrow(new RuntimeException("send error")).when(producer).send(anyList());

        rocketMqUtils.batchSendMessage("testTopic", messages);

        verify(producer, times(1)).send(anyList());
        // Expect logging of the error, but no exception rethrown
    }



    @Test
    void registerMessageListener_alreadyStarted() throws Exception {
        ReflectionTestUtils.setField(rocketMqUtils, "consumerStarted", new java.util.concurrent.atomic.AtomicBoolean(true));

        MessageListenerConcurrently listener = mock(MessageListenerConcurrently.class);
        rocketMqUtils.registerMessageListener(listener);

        verify(consumer, never()).setNamesrvAddr(anyString());
        verify(consumer, never()).subscribe(anyString(), anyString());
        verify(consumer, never()).registerMessageListener(any(MessageListenerConcurrently.class));
        verify(consumer, never()).start();
    }

    @Test
    void destroy_success() {
        rocketMqUtils.destroy();

        verify(producer, times(1)).shutdown();
        verify(consumer, times(1)).shutdown();
    }

    @Test
    void destroy_producerNull() {
        ReflectionTestUtils.setField(rocketMqUtils, "producer", null);

        rocketMqUtils.destroy();

        verify(producer, never()).shutdown();
        verify(consumer, times(1)).shutdown();
    }

    @Test
    void destroy_consumerNull() {
        ReflectionTestUtils.setField(rocketMqUtils, "consumer", null);

        rocketMqUtils.destroy();

        verify(producer, times(1)).shutdown();
        verify(consumer, never()).shutdown();
    }
}
