package com.erjean.carbatterywarning.model.vo;

import lombok.Data;

/**
 * 预警结果
 */
@Data
public class WarnResult {
    private Long carId;
    private String batteryType;
    private String warnName;
    private Integer warnLevel;
}
