package com.erjean.carbatterywarning.model.entity;

import lombok.Data;

/**
 * @TableName vehicle_info
 */
@Data
public class VehicleInfo {
    private Long id;

    private String vid;

    private Long carId;

    private Integer batteryType;

    private Integer totalMileage;

    private Integer batteryHealth;

    private static final long serialVersionUID = 1L;

}