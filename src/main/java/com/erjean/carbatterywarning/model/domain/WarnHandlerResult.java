package com.erjean.carbatterywarning.model.domain;

import lombok.Data;

/**
 * 预警结果
 */
@Data
public class WarnHandlerResult {
    private Integer level;
    private String warnName;
}
