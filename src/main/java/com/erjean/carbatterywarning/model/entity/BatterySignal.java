package com.erjean.carbatterywarning.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName battery_signal
 */
@Data
public class BatterySignal implements Serializable {
    private Long id;

    private String vid;

    private Long carId;

    private Integer batteryType;

    private Object signal;

    private Date reportTime;

    private Integer processed;

    private Date processedTime;

    private static final long serialVersionUID = 1L;
}