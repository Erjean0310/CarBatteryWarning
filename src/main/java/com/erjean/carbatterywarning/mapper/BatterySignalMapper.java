package com.erjean.carbatterywarning.mapper;

import com.erjean.carbatterywarning.model.entity.BatterySignal;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface BatterySignalMapper {
    /**
     * 插入电池信号到数据库
     *
     * @param batterySignal 信号信息
     * @return 插入的行数
     */
    int insert(BatterySignal batterySignal);

    /**
     * 根据vid查询信号信息
     *
     * @param vid 车辆id
     * @return 信号信息列表
     */
    List<BatterySignal> selectByVid(String vid);

    /**
     * 根据时间查询信号信息
     *
     * @param date 查询时间
     * @return 信号信息列表
     */
    List<BatterySignal> selectSignalsByDate(Date date);

    /**
     * 更新信号处理状态
     *
     * @param id            信号id
     * @param processedTime 处理时间
     * @return 更新的行数
     */
    int updateProcessState(@Param("id") Long id, @Param("processedTime") Date processedTime);

    /**
     * 根据vid查询最新信号信息
     *
     * @param vid 车辆id
     * @return 最新信号信息
     */
    BatterySignal selectLatestSignalByVid(String vid);

    /**
     * 根据id列表查询信号信息
     *
     * @param ids 信号id列表
     * @return 信号信息列表
     */
    List<BatterySignal> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 批量更新信号处理状态
     *
     * @param ids         信号id列表
     * @param processTime 处理时间
     * @return 更新的行数
     */
    void batchUpdateProcessState(@Param("ids") List<Long> ids, @Param("processTime") Date processTime);

}




