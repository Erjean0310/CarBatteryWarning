package com.erjean.carbatterywarning.job;

import com.erjean.carbatterywarning.loader.WarnDataLoader;
import com.erjean.carbatterywarning.mapper.BatterySignalMapper;
import com.erjean.carbatterywarning.model.domain.Rule;
import com.erjean.carbatterywarning.model.domain.Signal;
import com.erjean.carbatterywarning.model.domain.WarnHandlerResult;
import com.erjean.carbatterywarning.model.domain.WarnRuleData;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.model.enums.BatteryTypeEnum;
import com.erjean.carbatterywarning.utils.RocketMqUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class WarnConsumer {

    @Resource
    private RocketMqUtils rocketMqUtils;

    @Resource
    private BatterySignalMapper batterySignalMapper;

    @Resource
    private WarnDataLoader warnDataLoader;

    @Resource
    private ObjectMapper cleanObjectMapper;

    @PostConstruct
    public void init() throws Exception {
        rocketMqUtils.registerMessageListener((msgs, context) -> {
            // 处理过的信号id
            List<Long> ids = new ArrayList<>();

            for (MessageExt msg : msgs) {
                try {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    BatterySignal batterySignal = cleanObjectMapper.readValue(json, BatterySignal.class);

                    log.info("接收到数据: {}", batterySignal);

                    // 提取字段
                    Long id = batterySignal.getId();
                    String vid = batterySignal.getVid();
                    Long carId = batterySignal.getCarId();
                    Integer batteryType = batterySignal.getBatteryType();
                    String signalStr = (String) batterySignal.getSignal();
                    Date reportTime = batterySignal.getReportTime();

                    Signal signal;
                    try {
                        signal = cleanObjectMapper.readValue(signalStr, Signal.class);
                    } catch (Exception e) {
                        log.error("无法解析信号数据: {}", signalStr, e);
                        continue;
                    }

                    // 获取报警规则
                    Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> warnRules = warnDataLoader.getWarnRules();
                    BatteryTypeEnum batteryTypeEnum = BatteryTypeEnum.fromValue(batteryType);
                    Map<Integer, WarnRuleData> warnRuleDataMap = warnRules.get(batteryTypeEnum);

                    // 处理 Mx - Mi 差值
                    Float mx = signal.getMx();
                    Float mi = signal.getMi();
                    if (mx != null && mi != null) {
                        Float mDiff = mx - mi;
                        WarnHandlerResult warnHandlerResult = judgeWarn(mDiff, warnRuleDataMap.get(1));
                        if (warnHandlerResult != null) {
                            log.info("【警报】vid: {}, 车架编号: {}, 电池类型: {}, 信号上报时间: {}, 报警类型: {}, 报警等级: {}",
                                    vid, carId, batteryTypeEnum.getName(), reportTime,
                                    warnHandlerResult.getWarnName(), warnHandlerResult.getLevel());
                        }
                    }

                    // 处理 Ix - Ii 差值
                    Float ix = signal.getIx();
                    Float ii = signal.getIi();
                    if (ix != null && ii != null) {
                        Float iDiff = ix - ii;
                        WarnHandlerResult warnHandlerResult = judgeWarn(iDiff, warnRuleDataMap.get(2));
                        if (warnHandlerResult != null) {
                            log.info("【警报】vid: {}, 车架编号: {}, 电池类型: {}, 信号上报时间: {}, 报警类型: {}, 报警等级: {}",
                                    vid, carId, batteryTypeEnum.getName(), reportTime,
                                    warnHandlerResult.getWarnName(), warnHandlerResult.getLevel());
                        }
                    }

                    ids.add(id);
                } catch (Exception e) {
                    log.error("消费消息失败", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }

            // 批量更新数据库
            if (!ids.isEmpty()) {
                try {
                    batterySignalMapper.batchUpdateProcessState(ids, new Date());
                    log.info("批量更新 {} 条记录", ids.size());
                } catch (Exception e) {
                    log.error("批量更新失败", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
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
