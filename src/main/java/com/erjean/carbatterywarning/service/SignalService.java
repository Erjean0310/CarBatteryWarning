package com.erjean.carbatterywarning.service;

import com.erjean.carbatterywarning.model.dto.SignalReportRequest;
import com.erjean.carbatterywarning.model.dto.WarningReportRequest;
import com.erjean.carbatterywarning.model.entity.BatterySignal;

import java.util.List;

/**
 *
 */
public interface SignalService {
    /**
     * 上报电池信息
     * @param request 信号信息
     * @return 存储的信息 id
     */
    Long report(SignalReportRequest request);

    BatterySignal getSignalByVid(String vid);

    void warn(List<WarningReportRequest> requests);
}
