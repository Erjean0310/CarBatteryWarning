package com.erjean.carbatterywarning.mapper;

public interface VehicleInfoMapper {

    /**
     * 根据车架 id 获取电池类型
     *
     * @param carId 车架 id
     * @return 电池类型
     */
    Integer getBatteryTypeByCarId(Long carId);
}




