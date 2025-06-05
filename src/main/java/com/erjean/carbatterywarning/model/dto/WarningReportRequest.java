package com.erjean.carbatterywarning.model.dto;

import com.erjean.carbatterywarning.model.domain.Signal;
import lombok.Data;

/**
 *
 */
@Data
public class WarningReportRequest {
    private Long carId;
    private Long warnId;
    private Signal signal;
}
