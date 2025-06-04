package com.erjean.carbatterywarning.model.entity;

import lombok.Data;

/**
 * @TableName vehicle_info
 */
@Data
public class VehicleInfo {
    private Long id;

    private String vid;

    private Long cardId;

    private Integer batteryType;

    private Integer totalMileage;

    private Integer batteryHealth;
}