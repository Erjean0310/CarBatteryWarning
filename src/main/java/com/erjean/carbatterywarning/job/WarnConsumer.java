package com.erjean.carbatterywarning.job;

import com.erjean.carbatterywarning.common.ErrorCode;
import com.erjean.carbatterywarning.exception.BusinessException;
import com.erjean.carbatterywarning.loader.WarnDataLoader;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.model.domain.Rule;
import com.erjean.carbatterywarning.model.domain.Signal;
import com.erjean.carbatterywarning.model.domain.WarnHandlerResult;
import com.erjean.carbatterywarning.model.domain.WarnRuleData;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.model.enums.BatteryTypeEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "warn-cg", topic = "warn-topic", consumeMode = ConsumeMode.CONCURRENTLY, messageModel = MessageModel.CLUSTERING)
public class WarnConsumer implements RocketMQListener<BatterySignal> {
    @Resource
    private BatterySignalMapper batterySignalMapper;
    @Resource
    private WarnDataLoader warnDataLoader;
    @Resource
    private ObjectMapper cleanObjectMapper;

    @Override
    public void onMessage(BatterySignal batterySignal) {
        log.info("接收到数据: {}", batterySignal);
        // 处理数据
        Long id = batterySignal.getId();
        String vid = batterySignal.getVid();
        Long carId = batterySignal.getCarId();
        Integer batteryType = batterySignal.getBatteryType();
        String signalStr = (String) batterySignal.getSignal();
        Signal signal;
        try {
            signal = cleanObjectMapper.readValue(signalStr, Signal.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法解析信号数据");
        }
        Date reportTime = batterySignal.getReportTime();

        Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> warnRules = warnDataLoader.getWarnRules();
        BatteryTypeEnum batteryTypeEnum = BatteryTypeEnum.fromValue(batteryType);
        Map<Integer, WarnRuleData> warnRuleDataMap = warnRules.get(batteryTypeEnum);
        Float mx = signal.getMx();
        Float mi = signal.getMi();
        if (mx != null && mi != null) {
            Float mDiff = mx - mi;
            WarnHandlerResult warnHandlerResult = judgeWarn(mDiff, warnRuleDataMap.get(1));
            if (warnHandlerResult != null) {
                // 日志打印
                log.info("【警报】vid: {}, 车架编号: {}, 电池类型: {}, 信号上报时间: {}, 报警类型: {}, 报警等级: {}",
                        vid, carId, batteryTypeEnum.getName(), reportTime, warnHandlerResult.getWarnName(),
                        warnHandlerResult.getLevel());
            }
        }

        Float ix = signal.getIx();
        Float ii = signal.getIi();
        if (ix != null && ii != null) {
            Float iDiff = ix - ii;
            WarnHandlerResult warnHandlerResult = judgeWarn(iDiff, warnRuleDataMap.get(2));
            if (warnHandlerResult != null) {
                log.info("【警报】vid: {}, 车架编号: {}, 电池类型: {}, 信号上报时间: {}, 报警类型: {}, 报警等级: {}",
                        vid, carId, batteryTypeEnum.getName(), reportTime, warnHandlerResult.getWarnName(),
                        warnHandlerResult.getLevel());
            }
        }

        // 更新数据库
        batterySignalMapper.updateProcessState(id, new Date());

    }

    public WarnHandlerResult judgeWarn(Float diff, WarnRuleData warnRuleData) {
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
            WarnHandlerResult warnHandlerResult = new WarnHandlerResult();
            warnHandlerResult.setLevel(warnLevel);
            warnHandlerResult.setWarnName(warnRuleData.getWarnName());
            return warnHandlerResult;
        }
        return null;
    }
}
