package com.erjean.carbatterywarning.model.domain;

import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
public class WarnRuleData {
    List<Rule> rules;
    String warnName;
}
