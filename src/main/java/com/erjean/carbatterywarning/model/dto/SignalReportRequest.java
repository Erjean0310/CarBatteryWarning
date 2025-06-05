package com.erjean.carbatterywarning.model.dto;


import com.erjean.carbatterywarning.model.domain.Signal;
import lombok.Data;

/**
 * 电池信号上报请求
 */
@Data
public class SignalReportRequest {
    private String vid;
    private Integer batteryType;
    private Long carId;
    private Signal signal;
}
