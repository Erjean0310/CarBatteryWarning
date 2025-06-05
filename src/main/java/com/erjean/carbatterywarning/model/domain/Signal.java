package com.erjean.carbatterywarning.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 读取的信号数据
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Signal {
    @JsonProperty("Mx")
    private Float Mx;

    @JsonProperty("Mi")
    private Float Mi;

    @JsonProperty("Ix")
    private Float Ix;

    @JsonProperty("Ii")
    private Float Ii;
}
