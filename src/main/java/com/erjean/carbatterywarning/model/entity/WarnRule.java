package com.erjean.carbatterywarning.model.entity;

import lombok.Data;

/**
 * @TableName warn_rule
 */
@Data
public class WarnRule {
    private Integer id;

    private Integer warnId;

    private String ruleName;

    private Integer batteryType;

    private Object rule;
}