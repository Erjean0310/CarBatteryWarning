package com.erjean.carbatterywarning.model.dto;

import com.erjean.carbatterywarning.model.domain.Signal;
import lombok.Data;

/**
 * 预警上报请求
 */
@Data
public class WarningReportRequest {
    private Long carId;
    private Long warnId;
    private Signal signal;
}
