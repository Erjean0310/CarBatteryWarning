package com.erjean.carbatterywarning.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//
///**
// *
// */
//@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class Signal {
//    @JsonProperty("Mx")
//    private Float maxVoltage;
//
//    @JsonProperty("Mi")
//    private Float minVoltage;
//
//    @JsonProperty("Ix")
//    private Float maxCurrent;
//
//    @JsonProperty("Ii")
//    private Float minCurrent;
//
//}