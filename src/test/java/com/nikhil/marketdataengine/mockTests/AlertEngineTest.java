package com.nikhil.marketdataengine.mockTests;

import com.nikhil.marketdataengine.model.AlertCondition;
import com.nikhil.marketdataengine.model.AlertRule;
import com.nikhil.marketdataengine.repository.AlertRuleRepository;
import com.nikhil.marketdataengine.repository.TriggeredAlertRepository;
import com.nikhil.marketdataengine.service.AlertEngine;
import com.nikhil.marketdataengine.service.BinanceWebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlertEngineTest {
    private AlertEngine alertEngine;

    @BeforeEach
    void setup(){
        alertEngine = new AlertEngine(
                Mockito.mock(AlertRuleRepository.class),
                Mockito.mock(BinanceWebSocketService.class),
                Mockito.mock(TriggeredAlertRepository.class)
        );
    }

    @Test
    void testIsTriggered_AboveCondition_PriceIsHiger_ReturnsTrue(){
        AlertRule alertRule = AlertRule.builder()
                .alertCondition(AlertCondition.ABOVE)
                .threshold(100.0)
                .build();

        boolean result = alertEngine.isTriggered(alertRule, 105.0);
        assertTrue(result, "Should trigger when price is higher than 100");
    }

    @Test
    void testIsTriggered_BelowCondition_PriceIsLower_ReturnsTrue(){
        AlertRule alertRule = AlertRule.builder()
                .alertCondition(AlertCondition.BELOW)
                .threshold(100.0)
                .build();

        boolean result = alertEngine.isTriggered(alertRule, 95.0);
        assertTrue(result, "Should trigger when price is lower than 100");
    }

    @Test
    void testIsNotTriggered_AboveCondition_PriceIsHiger_ReturnsFalse(){
        AlertRule alertRule = AlertRule.builder()
                .alertCondition(AlertCondition.ABOVE)
                .threshold(100.0)
                .build();

        boolean result = alertEngine.isTriggered(alertRule, 95.0);
        assertFalse(result, "Should not trigger when price is lower than 100");
    }

    @Test
    void testIsNotTriggered_BelowCondition_PriceIsLower_ReturnsFalse(){
        AlertRule alertRule = AlertRule.builder()
                .alertCondition(AlertCondition.BELOW)
                .threshold(100.0)
                .build();

        boolean result = alertEngine.isTriggered(alertRule, 105.0);
        assertFalse(result, "Should not trigger when price is higher than 100");
    }
}
