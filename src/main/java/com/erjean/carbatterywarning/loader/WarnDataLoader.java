package com.erjean.carbatterywarning.loader;

import com.erjean.carbatterywarning.model.enums.BatteryTypeEnum;
import com.erjean.carbatterywarning.mapper.WarnRuleMapper;
import com.erjean.carbatterywarning.model.domain.Rule;
import com.erjean.carbatterywarning.model.entity.WarnRule;
import com.erjean.carbatterywarning.model.domain.WarnRuleData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载预警规则
 */
@Component
public class WarnDataLoader implements CommandLineRunner {
    @Resource
    private WarnRuleMapper warnRuleMapper;
    @Resource
    private ObjectMapper objectMapper;

    // 存储预警规则
    private final Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> warnRuleMap = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        loadWarnData();
    }

    /**
     * 加载预警规则
     */
    private void loadWarnData() throws JsonProcessingException {
        // 获取所有预警规则
        List<WarnRule> warnRules = warnRuleMapper.getAllWarnRule();

        for (WarnRule warnRule : warnRules) {
            Rule[] ruleArray = objectMapper.readValue((String) warnRule.getRule(), Rule[].class);
            WarnRuleData warnRuleData = new WarnRuleData();
            warnRuleData.setWarnName(warnRule.getRuleName());
            warnRuleData.setRules(Arrays.asList(ruleArray));

            BatteryTypeEnum batteryTypeEnum = BatteryTypeEnum.fromValue(warnRule.getBatteryType());
            warnRuleMap.computeIfAbsent(batteryTypeEnum, k -> new HashMap<>()).put(warnRule.getWarnId(), warnRuleData);
        }
    }

    // 获取预警规则
    public Map<BatteryTypeEnum, Map<Integer, WarnRuleData>> getWarnRules() {
        return warnRuleMap;
    }
}
