package com.erjean.carbatterywarning.model.dto;


import com.erjean.carbatterywarning.model.entity.Signal;
import lombok.Data;

@Data
public class SignalReportRequest {
    private String vid;
    private Integer batteryType;
    private Long carId;
    private Signal signal;
}
