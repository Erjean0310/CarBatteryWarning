package com.erjean.carbatterywarning.mapper;

import com.erjean.carbatterywarning.model.entity.WarnRule;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WarnRuleMapperTest {

    @Mock
    private WarnRuleMapper warnRuleMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllWarnRule() {
        List<WarnRule> mockList = Collections.singletonList(new WarnRule());
        when(warnRuleMapper.getAllWarnRule()).thenReturn(mockList);

        List<WarnRule> result = warnRuleMapper.getAllWarnRule();

        assertEquals(mockList, result);
        verify(warnRuleMapper, times(1)).getAllWarnRule();
    }
}
