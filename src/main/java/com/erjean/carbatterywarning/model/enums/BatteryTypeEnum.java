package com.erjean.carbatterywarning.model.enums;

import com.erjean.carbatterywarning.common.ErrorCode;
import com.erjean.carbatterywarning.exception.BusinessException;

/**
 * 电池类型枚举
 */
public enum BatteryTypeEnum {
    /**
     * 三元电池
     */
    TERNARY(0, "三元电池"),

    /**
     * 铁锂电池
     */
    LITHIUM_IRON(1, "铁锂电池");

    private final int value;
    private final String name;

    BatteryTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据值获取对应的枚举实例
     *
     * @param value 枚举对应的整数值
     * @return BatteryTypeEnum 实例
     * @throws BusinessException 如果没有匹配的枚举值
     */
    public static BatteryTypeEnum fromValue(int value) {
        for (BatteryTypeEnum type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知电池类型");
    }
}
