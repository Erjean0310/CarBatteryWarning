package com.erjean.carbatterywarning.model.domain;

import lombok.Data;

import java.util.List;

/**
 * 规则数据
 */
@Data
public class WarnRuleData {
    List<Rule> rules;
    String warnName;
}
