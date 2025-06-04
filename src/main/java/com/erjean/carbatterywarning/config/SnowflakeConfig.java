package com.erjean.carbatterywarning.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class SnowflakeConfig {
    @Bean
    public SnowflakeGenerator  snowflakeGenerator() {
        return new SnowflakeGenerator();
    }
}
