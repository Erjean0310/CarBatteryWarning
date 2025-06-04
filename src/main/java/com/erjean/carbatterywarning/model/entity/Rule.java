package com.erjean.carbatterywarning.model.entity;

import lombok.Data;

/**
 * @TableName rule
 */
@Data
public class Rule {
    private Integer id;

    private Integer ruleId;

    private String ruleName;

    private Integer batteryType;

    private Object warningRule;
}