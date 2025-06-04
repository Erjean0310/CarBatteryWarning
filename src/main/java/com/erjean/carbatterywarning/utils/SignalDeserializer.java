package com.erjean.carbatterywarning.utils;

import com.erjean.carbatterywarning.model.entity.Signal;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class SignalDeserializer extends JsonDeserializer<Signal> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Signal deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String json = p.getValueAsString();
        return objectMapper.readValue(json, Signal.class);
    }
}
