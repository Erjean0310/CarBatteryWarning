package com.erjean.carbatterywarning.mapper;

import com.erjean.carbatterywarning.model.entity.WarnRule;

import java.util.List;

public interface WarnRuleMapper {
    /**
     * 查询所有告警规则
     *
     * @return 告警规则列表
     */
    List<WarnRule> getAllWarnRule();
}




