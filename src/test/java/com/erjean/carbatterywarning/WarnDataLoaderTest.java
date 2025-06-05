package com.erjean.carbatterywarning;

import com.erjean.carbatterywarning.loader.WarnDataLoader;
import com.erjean.carbatterywarning.mapper.WarnRuleMapper;
import com.erjean.carbatterywarning.model.domain.Rule;
import com.erjean.carbatterywarning.model.domain.WarnRuleData;
import com.erjean.carbatterywarning.model.entity.WarnRule;
import com.erjean.carbatterywarning.model.enums.BatteryTypeEnum;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class WarnDataLoaderTest {


}
