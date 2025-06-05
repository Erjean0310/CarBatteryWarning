package com.erjean.carbatterywarning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.erjean.carbatterywarning.common.ErrorCode;
import com.erjean.carbatterywarning.exception.BusinessException;
import com.erjean.carbatterywarning.exception.ThrowUtil;
import com.erjean.carbatterywarning.loader.WarnDataLoader;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.mapper.VehicleInfoMapper;
import com.erjean.carbatterywarning.model.dto.SignalReportRequest;
import com.erjean.carbatterywarning.model.dto.WarningReportRequest;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.model.domain.Rule;
import com.erjean.carbatterywarning.model.domain.WarnRuleData;
import com.erjean.carbatterywarning.model.enums.BatteryTypeEnum;
import com.erjean.carbatterywarning.model.vo.WarnResult;
import com.erjean.carbatterywarning.service.SignalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


/**
 *
 */
@Service
public class SignalServiceImpl implements SignalService {
    @Resource
    private BatterySignalMapper batterySignalMapper;
    @Resource
    private SnowflakeGenerator snowflakeGenerator;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private VehicleInfoMapper vehicleInfoMapper;
    @Resource
    private WarnDataLoader warnDataLoader;

    /**
     * 上报电池信息
     *
     * @param request 信号信息
     * @return 存储的信息 id
     */
    @Override
    public Long report(SignalReportRequest request) {
        BatterySignal batterySignal = new BatterySignal();
        BeanUtil.copyProperties(request, batterySignal);
        // 雪花算法生成 id
        Long id = snowflakeGenerator.next();
        batterySignal.setId(id);
        // 初始为未处理状态
        batterySignal.setProcessed(0);
        // 设置上报时间
        batterySignal.setReportTime(new Date());
        // 将信号转换为字符串
        try {
            batterySignal.setSignal(objectMapper.writeValueAsString(request.getSignal()));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法解析信号数据");
        }
        // 保存到数据库
        int result = batterySignalMapper.insert(batterySignal);
        ThrowUtil.throwIf(result != 1, ErrorCode.OPERATION_ERROR, "保存信号的数据库出错");
        // 返回 id
        return id;
    }

    /**
     * 根据 vid 查询电池信号
     *
     * @param vid 车辆 vid
     * @return 信号记录
     */
    @Override
    public List<BatterySignal> listSignalsByVid(String vid) {
        List<BatterySignal> batterySignalList = batterySignalMapper.selectByVid(vid);
        return batterySignalList;
    }

    @Override
    public List<WarnResult> warn(List<WarningReportRequest> requests) {
        Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> warnRules = warnDataLoader.getWarnRules();

        List<WarnResult> results = new ArrayList<>();
        for (WarningReportRequest request : requests) {
            Integer batteryTypeValue = vehicleInfoMapper.getBatteryTypeByCarId(request.getCarId());
            ThrowUtil.throwIf(batteryTypeValue == null, ErrorCode.NOT_FOUND_ERROR, "车架信息不存在");
            BatteryTypeEnum batteryTypeEnum = BatteryTypeEnum.fromValue(batteryTypeValue);
            Map<Integer, WarnRuleData> warnRuleDataMap = warnRules.get(batteryTypeEnum);

            // 判断电压差报警
            if (request.getWarnId() == null || request.getWarnId() == 1) {
                Float mx = request.getSignal().getMx();
                Float mi = request.getSignal().getMi();
                if (mx != null && mi != null) {
                    Float mDiff = mx - mi;
                    WarnRuleData warnRuleData = warnRuleDataMap.get(1);
                    WarnResult warnResult = judgeWarn(mDiff, warnRuleData, request.getCarId(), batteryTypeEnum);
                    if (warnResult != null) {
                        results.add(warnResult);
                    }
                }
            }
            // 判断电流差报警
            if (request.getWarnId() == null || request.getWarnId() == 2) {
                Float ix = request.getSignal().getIx();
                Float ii = request.getSignal().getIi();
                if (ix != null && ii != null) {
                    Float iDiff = ix - ii;
                    WarnRuleData warnRuleData = warnRuleDataMap.get(2);
                    WarnResult warnResult = judgeWarn(iDiff, warnRuleData, request.getCarId(), batteryTypeEnum);
                    if (warnResult != null) {
                        results.add(warnResult);
                    }
                }
            }
        }
        return results;
    }

    private WarnResult judgeWarn(Float diff, WarnRuleData warnRuleData, Long carId, BatteryTypeEnum batteryTypeEnum) {
        Integer warnLevel = -1;
        for (Rule rule : warnRuleData.getRules()) {
            Float max = Optional.ofNullable(rule.getMax()).orElse(Float.MAX_VALUE);
            Float min = Optional.ofNullable(rule.getMin()).orElse(Float.MIN_VALUE);
            if (diff >= min && diff < max) {
                warnLevel = rule.getLevel();
                break;
            }
        }
        if (warnLevel != -1) {
            WarnResult warnResult = new WarnResult();
            warnResult.setCarId(carId);
            warnResult.setBatteryType(batteryTypeEnum.getName());
            warnResult.setWarnName(warnRuleData.getWarnName());
            warnResult.setWarnLevel(warnLevel);
            return warnResult;
        }
        return null;
    }
}
