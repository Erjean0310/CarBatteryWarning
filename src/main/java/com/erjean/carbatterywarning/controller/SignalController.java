package com.erjean.carbatterywarning.controller;

import com.erjean.carbatterywarning.common.BaseResponse;
import com.erjean.carbatterywarning.common.ResultUtil;
import com.erjean.carbatterywarning.model.dto.SignalReportRequest;
import com.erjean.carbatterywarning.model.dto.WarningReportRequest;
import com.erjean.carbatterywarning.model.entity.BatterySignal;
import com.erjean.carbatterywarning.model.vo.WarnResult;
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


    /**
     * 根据车辆vid查询车辆信号
     * @param vid 车辆vid
     * @return 车辆信号
     */
    @GetMapping("/signal/{vid}")
    public BaseResponse<BatterySignal> listSignalsByVid(@PathVariable String vid) {
        BatterySignal batterySignal = signalService.getSignalByVid(vid);
        return ResultUtil.success(batterySignal);
    }

    /**
     * 告警接口
     * @param requests 告警请求参数
     * @return 告警结果
     */
    @PostMapping("/warn")
    public BaseResponse<List<WarnResult>> warn(@RequestBody List<WarningReportRequest> requests) {
        List<WarnResult> results = signalService.warn(requests);
        return ResultUtil.success(results);
    }
}
