package com.erjean.carbatterywarning.mapper;

import com.erjean.carbatterywarning.model.entity.BatterySignal;

public interface BatterySignalMapper {
    /**
     * 插入电池信号到数据库
     * @param batterySignal 信号信息
     * @return 插入的行数
     */
    int insert(BatterySignal batterySignal);
}




