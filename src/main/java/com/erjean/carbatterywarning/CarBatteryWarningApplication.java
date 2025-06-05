package com.erjean.carbatterywarning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.erjean.carbatterywarning.mapper")
public class CarBatteryWarningApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarBatteryWarningApplication.class, args);
    }

}
