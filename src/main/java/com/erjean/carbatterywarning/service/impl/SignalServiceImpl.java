package com.erjean.carbatterywarning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.erjean.carbatterywarning.common.ErrorCode;
import com.erjean.carbatterywarning.exception.BusinessException;
import com.erjean.carbatterywarning.exception.ThrowUtil;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.model.dto.SignalReportRequest;
import com.erjean.carbatterywarning.model.dto.WarningReportRequest;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.model.entity.Signal;
import com.erjean.carbatterywarning.service.SignalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;



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

    /**
     * 上报电池信息
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

    @Override
    public BatterySignal getSignalByVid(String vid) {
        return null;
    }

    @Override
    public void warn(List<WarningReportRequest> requests) {

    }
}
