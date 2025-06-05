package com.erjean.carbatterywarning.model.domain;

import lombok.Data;

/**
 * 预警规则
 */
@Data
public class Rule {
    /**
     * 规则为左闭右开，即 min <= x < max
     */
    private Float max;
    private Float min;

    /**
     * 报警等级 1-4， -1 为不报警
     */
    private Integer level;
}
