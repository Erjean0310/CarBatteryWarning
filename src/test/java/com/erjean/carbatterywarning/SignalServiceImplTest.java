package com.erjean.carbatterywarning;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.erjean.carbatterywarning.common.ErrorCode;
import com.erjean.carbatterywarning.constant.RedisConstants;
import com.erjean.carbatterywarning.exception.BusinessException;
import com.erjean.carbatterywarning.loader.WarnDataLoader;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.mapper.VehicleInfoMapper;
import com.erjean.carbatterywarning.model.domain.Rule;
import com.erjean.carbatterywarning.model.domain.Signal;
import com.erjean.carbatterywarning.model.domain.WarnRuleData;
import com.erjean.carbatterywarning.model.dto.SignalReportRequest;
import com.erjean.carbatterywarning.model.dto.WarningReportRequest;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.model.enums.BatteryTypeEnum;
import com.erjean.carbatterywarning.model.vo.WarnResult;
import com.erjean.carbatterywarning.service.impl.SignalServiceImpl;
import com.erjean.carbatterywarning.utils.RedisUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignalServiceImplTest {

    @InjectMocks
    private SignalServiceImpl signalService;

    @Mock
    private BatterySignalMapper batterySignalMapper;

    @Mock
    private SnowflakeGenerator snowflakeGenerator;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private VehicleInfoMapper vehicleInfoMapper;

    @Mock
    private WarnDataLoader warnDataLoader;

    @Mock
    private RedisUtils redisUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void report_success() throws JsonProcessingException {
        SignalReportRequest request = new SignalReportRequest();
        request.setVid("testVid");
        request.setSignal(new Signal());

        Long generatedId = 123L;
        when(snowflakeGenerator.next()).thenReturn(generatedId);
        when(objectMapper.writeValueAsString(any())).thenReturn("signalJson");
        when(batterySignalMapper.insert(any(BatterySignal.class))).thenReturn(1);

        Long resultId = signalService.report(request);

        assertEquals(generatedId, resultId);
        verify(batterySignalMapper, times(1)).insert(any(BatterySignal.class));
        verify(redisUtils, times(1)).listRightPush(eq(RedisConstants.SIGNAL_PENDING_QUEUE_KEY), eq(generatedId));
        verify(redisUtils, times(1)).delete(eq(RedisConstants.LATEST_SIGNAL_KEY + "testVid"));
    }

    @Test
    void report_jsonProcessingException() throws JsonProcessingException {
        SignalReportRequest request = new SignalReportRequest();
        request.setVid("testVid");
        request.setSignal(new Signal());

        when(snowflakeGenerator.next()).thenReturn(123L);
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        BusinessException exception = assertThrows(BusinessException.class, () -> signalService.report(request));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("无法解析信号数据", exception.getMessage());
        verify(batterySignalMapper, never()).insert(any(BatterySignal.class));
        verify(redisUtils, never()).listRightPush(anyString(), any());
        verify(redisUtils, never()).delete(anyString());
    }

    @Test
    void report_databaseError() throws JsonProcessingException {
        SignalReportRequest request = new SignalReportRequest();
        request.setVid("testVid");
        request.setSignal(new Signal());

        when(snowflakeGenerator.next()).thenReturn(123L);
        when(objectMapper.writeValueAsString(any())).thenReturn("signalJson");
        when(batterySignalMapper.insert(any(BatterySignal.class))).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> signalService.report(request));
        assertEquals(ErrorCode.OPERATION_ERROR.getCode(), exception.getCode());
        assertEquals("保存信号的数据库出错", exception.getMessage());
        verify(batterySignalMapper, times(1)).insert(any(BatterySignal.class));
        verify(redisUtils, never()).listRightPush(anyString(), any());
        verify(redisUtils, never()).delete(anyString());
    }

    @Test
    void getSignalByVid_cacheHit() {
        String vid = "testVid";
        BatterySignal cachedSignal = new BatterySignal();
        when(redisUtils.get(eq(RedisConstants.LATEST_SIGNAL_KEY + vid))).thenReturn(cachedSignal);

        BatterySignal result = signalService.getSignalByVid(vid);

        assertEquals(cachedSignal, result);
        verify(redisUtils, times(1)).get(eq(RedisConstants.LATEST_SIGNAL_KEY + vid));
        verify(batterySignalMapper, never()).selectLatestSignalByVid(anyString());
        verify(redisUtils, never()).set(anyString(), any(), anyLong());
    }

    @Test
    void getSignalByVid_cacheMiss_databaseHit() {
        String vid = "testVid";
        BatterySignal dbSignal = new BatterySignal();
        when(redisUtils.get(eq(RedisConstants.LATEST_SIGNAL_KEY + vid))).thenReturn(null);
        when(batterySignalMapper.selectLatestSignalByVid(eq(vid))).thenReturn(dbSignal);

        BatterySignal result = signalService.getSignalByVid(vid);

        assertEquals(dbSignal, result);
        verify(redisUtils, times(1)).get(eq(RedisConstants.LATEST_SIGNAL_KEY + vid));
        verify(batterySignalMapper, times(1)).selectLatestSignalByVid(eq(vid));
        verify(redisUtils, times(1)).set(eq(RedisConstants.LATEST_SIGNAL_KEY + vid), eq(dbSignal), eq(60 * 60L));
    }

    @Test
    void getSignalByVid_cacheMiss_databaseMiss() {
        String vid = "testVid";
        when(redisUtils.get(eq(RedisConstants.LATEST_SIGNAL_KEY + vid))).thenReturn(null);
        when(batterySignalMapper.selectLatestSignalByVid(eq(vid))).thenReturn(null);

        BatterySignal result = signalService.getSignalByVid(vid);

        assertNull(result);
        verify(redisUtils, times(1)).get(eq(RedisConstants.LATEST_SIGNAL_KEY + vid));
        verify(batterySignalMapper, times(1)).selectLatestSignalByVid(eq(vid));
        verify(redisUtils, never()).set(anyString(), any(), anyLong());
    }


    @Test
    void warn_currentDiffWarn() {
        WarningReportRequest request = new WarningReportRequest();
        request.setCarId(1L);
        Signal signal = new Signal();
        signal.setIx(10.0f);
        signal.setIi(5.0f);
        request.setSignal(signal);
        request.setWarnId(2L);

        Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> warnRules = new HashMap<>();
        Map<Integer, WarnRuleData> batteryTypeRules = new HashMap<>();
        WarnRuleData currentRuleData = new WarnRuleData();
        currentRuleData.setWarnName("电流差报警");
        List<Rule> rules = new ArrayList<>();
        Rule rule1 = new Rule();
        rule1.setMin(3.0f);
        rule1.setMax(6.0f);
        rule1.setLevel(1);
        rules.add(rule1);
        Rule rule2 = new Rule();
        rule2.setMin(6.0f);
        rule2.setMax(Float.MAX_VALUE);
        rule2.setLevel(2);
        rules.add(rule2);
        currentRuleData.setRules(rules);
        batteryTypeRules.put(2, currentRuleData);
        warnRules.put(BatteryTypeEnum.LITHIUM_IRON, batteryTypeRules);

        when(vehicleInfoMapper.getBatteryTypeByCarId(eq(1L))).thenReturn(BatteryTypeEnum.LITHIUM_IRON.getValue());
        when(warnDataLoader.getWarnRules()).thenReturn(warnRules);

        List<WarnResult> results = signalService.warn(Collections.singletonList(request));

        assertNotNull(results);
        assertEquals(1, results.size());
        WarnResult warnResult = results.get(0);
        assertEquals(1L, warnResult.getCarId());
        assertEquals(BatteryTypeEnum.LITHIUM_IRON.getName(), warnResult.getBatteryType());
        assertEquals("电流差报警", warnResult.getWarnName());
        assertEquals(1, warnResult.getWarnLevel());
    }

    @Test
    void warn_vehicleInfoNotFound() {
        WarningReportRequest request = new WarningReportRequest();
        request.setCarId(1L);
        request.setSignal(new Signal());
        request.setWarnId(1L);

        when(vehicleInfoMapper.getBatteryTypeByCarId(eq(1L))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> signalService.warn(Collections.singletonList(request)));
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
        assertEquals("车架信息不存在", exception.getMessage());
    }

    @Test
    void warn_noWarnTriggered() {
        WarningReportRequest request = new WarningReportRequest();
        request.setCarId(1L);
        Signal signal = new Signal();
        signal.setMx(3.5f);
        signal.setMi(3.4f); // diff 0.1
        signal.setIx(2.0f);
        signal.setIi(1.9f); // diff 0.1
        request.setSignal(signal);
        request.setWarnId(null); // Check both voltage and current

        Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> warnRules = new HashMap<>();
        Map<Integer, WarnRuleData> batteryTypeRules = new HashMap<>();

        WarnRuleData voltageRuleData = new WarnRuleData();
        voltageRuleData.setWarnName("电压差报警");
        List<Rule> voltageRules = new ArrayList<>();
        Rule voltageRule = new Rule();
        voltageRule.setMin(1.0f);
        voltageRule.setMax(Float.MAX_VALUE);
        voltageRule.setLevel(1);
        voltageRules.add(voltageRule);
        voltageRuleData.setRules(voltageRules);
        batteryTypeRules.put(1, voltageRuleData);

        WarnRuleData currentRuleData = new WarnRuleData();
        currentRuleData.setWarnName("电流差报警");
        List<Rule> currentRules = new ArrayList<>();
        Rule currentRule = new Rule();
        currentRule.setMin(1.0f);
        currentRule.setMax(Float.MAX_VALUE);
        currentRule.setLevel(1);
        currentRules.add(currentRule);
        currentRuleData.setRules(currentRules);
        batteryTypeRules.put(2, currentRuleData);

        warnRules.put(BatteryTypeEnum.LITHIUM_IRON, batteryTypeRules);

        when(vehicleInfoMapper.getBatteryTypeByCarId(eq(1L))).thenReturn(BatteryTypeEnum.LITHIUM_IRON.getValue());
        when(warnDataLoader.getWarnRules()).thenReturn(warnRules);

        List<WarnResult> results = signalService.warn(Collections.singletonList(request));

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

}
