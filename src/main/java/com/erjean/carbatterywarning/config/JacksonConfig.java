package com.erjean.carbatterywarning.config;

import com.erjean.carbatterywarning.model.entity.Signal;
import com.erjean.carbatterywarning.utils.SignalDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(SignalDeserializer signalDeserializer) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Signal.class, signalDeserializer);
        mapper.registerModule(module);
        return mapper;
    }

    @Bean
    public SignalDeserializer signalDeserializer() {
        return new SignalDeserializer();
    }
}
