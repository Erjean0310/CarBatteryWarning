package com.erjean.carbatterywarning.controller;

import com.erjean.carbatterywarning.common.BaseResponse;
import com.erjean.carbatterywarning.common.ResultUtil;
import com.erjean.carbatterywarning.model.dto.SignalReportRequest;
import com.erjean.carbatterywarning.model.dto.WarningReportRequest;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.service.SignalService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@RestController
@RequestMapping("/api")
public class SignalController {
    @Resource
    private SignalService signalService;


    /**
     * 电池信号上报接口
     * @param signalReportRequest 上报信号数据
     * @return 上报是否成功
     */
    @PostMapping("report")
    public BaseResponse<Long> report(@RequestBody SignalReportRequest signalReportRequest) {
        Long id = signalService.report(signalReportRequest);
        return ResultUtil.success(id);
    }


    @GetMapping("/signal/{vid}")
    public BaseResponse<List<BatterySignal>> listSignalsByVid(@PathVariable String vid) {
        List<BatterySignal> batterySignal = signalService.listSignalsByVid(vid);
        return ResultUtil.success(batterySignal);
    }

    @PostMapping("/warn")
    public BaseResponse warn(@RequestBody List<WarningReportRequest> requests) {
        System.out.println(requests);
        signalService.warn(requests);
        return ResultUtil.success(null);
    }
}
