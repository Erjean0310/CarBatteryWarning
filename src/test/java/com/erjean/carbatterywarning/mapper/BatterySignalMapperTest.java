package com.erjean.carbatterywarning.mapper;

import com.erjean.carbatterywarning.model.entity.BatterySignal;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BatterySignalMapperTest {

    @Mock
    private BatterySignalMapper batterySignalMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void insert() {
        BatterySignal batterySignal = new BatterySignal();
        when(batterySignalMapper.insert(any(BatterySignal.class))).thenReturn(1);

        int result = batterySignalMapper.insert(batterySignal);

        assertEquals(1, result);
        verify(batterySignalMapper, times(1)).insert(any(BatterySignal.class));
    }

    @Test
    void selectByVid() {
        String vid = "testVid";
        List<BatterySignal> mockList = Collections.singletonList(new BatterySignal());
        when(batterySignalMapper.selectByVid(anyString())).thenReturn(mockList);

        List<BatterySignal> result = batterySignalMapper.selectByVid(vid);

        assertEquals(mockList, result);
        verify(batterySignalMapper, times(1)).selectByVid(eq(vid));
    }

    @Test
    void selectSignalsByDate() {
        Date date = new Date();
        List<BatterySignal> mockList = Collections.singletonList(new BatterySignal());
        when(batterySignalMapper.selectSignalsByDate(any(Date.class))).thenReturn(mockList);

        List<BatterySignal> result = batterySignalMapper.selectSignalsByDate(date);

        assertEquals(mockList, result);
        verify(batterySignalMapper, times(1)).selectSignalsByDate(eq(date));
    }

    @Test
    void updateProcessState() {
        Long id = 1L;
        Date processedTime = new Date();
        when(batterySignalMapper.updateProcessState(anyLong(), any(Date.class))).thenReturn(1);

        int result = batterySignalMapper.updateProcessState(id, processedTime);

        assertEquals(1, result);
        verify(batterySignalMapper, times(1)).updateProcessState(eq(id), eq(processedTime));
    }

    @Test
    void selectLatestSignalByVid() {
        String vid = "testVid";
        BatterySignal mockSignal = new BatterySignal();
        when(batterySignalMapper.selectLatestSignalByVid(anyString())).thenReturn(mockSignal);

        BatterySignal result = batterySignalMapper.selectLatestSignalByVid(vid);

        assertEquals(mockSignal, result);
        verify(batterySignalMapper, times(1)).selectLatestSignalByVid(eq(vid));
    }

    @Test
    void selectByIds() {
        List<Long> ids = Collections.singletonList(1L);
        List<BatterySignal> mockList = Collections.singletonList(new BatterySignal());
        when(batterySignalMapper.selectByIds(anyList())).thenReturn(mockList);

        List<BatterySignal> result = batterySignalMapper.selectByIds(ids);

        assertEquals(mockList, result);
        verify(batterySignalMapper, times(1)).selectByIds(eq(ids));
    }

    @Test
    void batchUpdateProcessState() {
        List<Long> ids = Collections.singletonList(1L);
        Date processTime = new Date();
        doNothing().when(batterySignalMapper).batchUpdateProcessState(anyList(), any(Date.class));

        batterySignalMapper.batchUpdateProcessState(ids, processTime);

        verify(batterySignalMapper, times(1)).batchUpdateProcessState(eq(ids), eq(processTime));
    }
}
