package com.erjean.carbatterywarning;

import com.erjean.carbatterywarning.constant.RedisConstants;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.utils.RocketMqUtils;
import com.erjean.carbatterywarning.utils.RedisUtils;
import com.erjean.carbatterywarning.job.WarnProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WarnProducerTest {

    @InjectMocks
    private WarnProducer warnProducer;

    @Mock
    private BatterySignalMapper batterySignalMapper;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private RocketMqUtils rocketMqUtils;

    @Mock
    private ObjectMapper cleanObjectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void run_queueEmpty() {
        when(redisUtils.listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY))).thenReturn(0L);

        warnProducer.run();

        verify(redisUtils, times(1)).listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY));
        verify(redisUtils, never()).listRange(anyString(), anyLong(), anyLong());
        verify(batterySignalMapper, never()).selectByIds(anyList());
        verify(rocketMqUtils, never()).batchSendMessage(anyString(), anyList());
        verify(redisUtils, never()).listTrim(anyString(), anyLong(), anyLong());
    }

    @Test
    void run_success() throws Exception {
        List<Object> signalIds = new ArrayList<>();
        signalIds.add(1L);
        signalIds.add(2L);
        signalIds.add(3L);

        List<BatterySignal> batterySignalList = new ArrayList<>();
        BatterySignal signal1 = new BatterySignal();
        signal1.setId(1L);
        BatterySignal signal2 = new BatterySignal();
        signal2.setId(2L);
        BatterySignal signal3 = new BatterySignal();
        signal3.setId(3L);
        batterySignalList.add(signal1);
        batterySignalList.add(signal2);
        batterySignalList.add(signal3);

        when(redisUtils.listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY))).thenReturn((long) signalIds.size());
        when(redisUtils.listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), anyLong(), anyLong())).thenReturn(signalIds);
        when(batterySignalMapper.selectByIds(anyList())).thenReturn(batterySignalList);
        when(cleanObjectMapper.writeValueAsBytes(any(BatterySignal.class))).thenReturn("{}".getBytes());
        doNothing().when(rocketMqUtils).batchSendMessage(anyString(), anyList());
        doNothing().when(redisUtils).listTrim(anyString(), anyLong(), anyLong());

        warnProducer.run();

        verify(redisUtils, times(1)).listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY));
        verify(redisUtils, times(1)).listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq(0L), eq((long) signalIds.size() - 1));
        verify(batterySignalMapper, times(1)).selectByIds(eq(Arrays.asList(1L, 2L, 3L)));
        verify(cleanObjectMapper, times(signalIds.size())).writeValueAsBytes(any(BatterySignal.class));
        verify(rocketMqUtils, times(1)).batchSendMessage(eq("warn-topic"), anyList());
        verify(redisUtils, times(1)).listTrim(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq((long) signalIds.size()), eq(-1L));
    }

    @Test
    void run_selectByIdsEmpty() throws JsonProcessingException {
        List<Object> signalIds = new ArrayList<>();
        signalIds.add(1L);

        when(redisUtils.listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY))).thenReturn((long) signalIds.size());
        when(redisUtils.listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), anyLong(), anyLong())).thenReturn(signalIds);
        when(batterySignalMapper.selectByIds(anyList())).thenReturn(Collections.emptyList());
        doNothing().when(redisUtils).listTrim(anyString(), anyLong(), anyLong());


        warnProducer.run();

        verify(redisUtils, times(1)).listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY));
        verify(redisUtils, times(1)).listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq(0L), eq((long) signalIds.size() - 1));
        verify(batterySignalMapper, times(1)).selectByIds(eq(Collections.singletonList(1L)));
        verify(cleanObjectMapper, never()).writeValueAsBytes(any(BatterySignal.class));
        verify(rocketMqUtils, never()).batchSendMessage(anyString(), anyList());
        verify(redisUtils, times(1)).listTrim(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq((long) signalIds.size()), eq(-1L));
    }

    @Test
    void run_serializationError() throws JsonProcessingException {
        List<Object> signalIds = new ArrayList<>();
        signalIds.add(1L);

        List<BatterySignal> batterySignalList = new ArrayList<>();
        BatterySignal signal1 = new BatterySignal();
        signal1.setId(1L);
        batterySignalList.add(signal1);

        when(redisUtils.listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY))).thenReturn((long) signalIds.size());
        when(redisUtils.listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), anyLong(), anyLong())).thenReturn(signalIds);
        when(batterySignalMapper.selectByIds(anyList())).thenReturn(batterySignalList);
        when(cleanObjectMapper.writeValueAsBytes(any(BatterySignal.class))).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("serialization error") {});
        doNothing().when(redisUtils).listTrim(anyString(), anyLong(), anyLong());


        warnProducer.run();

        verify(redisUtils, times(1)).listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY));
        verify(redisUtils, times(1)).listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq(0L), eq((long) signalIds.size() - 1));
        verify(batterySignalMapper, times(1)).selectByIds(eq(Collections.singletonList(1L)));
        verify(cleanObjectMapper, times(1)).writeValueAsBytes(any(BatterySignal.class));
        verify(rocketMqUtils, never()).batchSendMessage(anyString(), anyList()); // Message should not be sent
        verify(redisUtils, times(1)).listTrim(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq((long) signalIds.size()), eq(-1L));
    }

    @Test
    void run_batchSendMessageError() throws JsonProcessingException {
        List<Object> signalIds = new ArrayList<>();
        signalIds.add(1L);

        List<BatterySignal> batterySignalList = new ArrayList<>();
        BatterySignal signal1 = new BatterySignal();
        signal1.setId(1L);
        batterySignalList.add(signal1);

        when(redisUtils.listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY))).thenReturn((long) signalIds.size());
        when(redisUtils.listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), anyLong(), anyLong())).thenReturn(signalIds);
        when(batterySignalMapper.selectByIds(anyList())).thenReturn(batterySignalList);
        when(cleanObjectMapper.writeValueAsBytes(any(BatterySignal.class))).thenReturn("{}".getBytes());
        doThrow(new RuntimeException("send message error")).when(rocketMqUtils).batchSendMessage(anyString(), anyList());
        doNothing().when(redisUtils).listTrim(anyString(), anyLong(), anyLong());


        warnProducer.run();

        verify(redisUtils, times(1)).listSize(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY));
        verify(redisUtils, times(1)).listRange(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq(0L), eq((long) signalIds.size() - 1));
        verify(batterySignalMapper, times(1)).selectByIds(eq(Collections.singletonList(1L)));
        verify(cleanObjectMapper, times(1)).writeValueAsBytes(any(BatterySignal.class));
        verify(rocketMqUtils, times(1)).batchSendMessage(eq("warn-topic"), anyList());
        verify(redisUtils, times(1)).listTrim(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq((long) signalIds.size()), eq(-1L));
    }
}
