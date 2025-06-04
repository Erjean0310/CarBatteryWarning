package com.erjean.carbatterywarning.model.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName battery_signal
 */
@Data
public class BatterySignal {
    private Long id;

    private String vid;

    private Long carId;

    private Integer batteryType;

    private Object signal;

    private Date reportTime;

    private Integer processed;

    private Date processedTime;
}