package com.erjean.carbatterywarning.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class VehicleInfoMapperTest {

    @Mock
    private VehicleInfoMapper vehicleInfoMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getBatteryTypeByCarId() {
        Long carId = 123L;
        Integer mockBatteryType = 1; // Example battery type value
        when(vehicleInfoMapper.getBatteryTypeByCarId(anyLong())).thenReturn(mockBatteryType);

        Integer result = vehicleInfoMapper.getBatteryTypeByCarId(carId);

        assertEquals(mockBatteryType, result);
        verify(vehicleInfoMapper, times(1)).getBatteryTypeByCarId(eq(carId));
    }
}
