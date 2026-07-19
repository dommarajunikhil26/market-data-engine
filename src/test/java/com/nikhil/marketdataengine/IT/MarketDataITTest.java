package com.nikhil.marketdataengine.IT;

import com.nikhil.marketdataengine.dto.AlertRuleRequestDto;
import com.nikhil.marketdataengine.model.AlertCondition;
import com.nikhil.marketdataengine.model.AlertRule;
import com.nikhil.marketdataengine.service.BinanceWebSocketService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "de.flapdoodle.mongodb.embedded.version=6.0.1"
        }
)
public class MarketDataITTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public BinanceWebSocketService binanceWebSocketService() {
            BinanceWebSocketService mock = Mockito.mock(BinanceWebSocketService.class);
            Mockito.when(mock.getPriceStream()).thenReturn(Flux.never());
            return mock;
        }
    }

    @Test
    void createAlertRule_returnsCreatedRule(){
        AlertRuleRequestDto dto = new AlertRuleRequestDto();
        dto.setSymbol("BTCUSDT");
        dto.setAlertCondition(AlertCondition.ABOVE);
        dto.setThreshold(60000.0);
        webTestClient.post()
                .uri("http://localhost:"+port+"/api/alerts")
                .body(Mono.just(dto), AlertRuleRequestDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AlertRule.class)
                .consumeWith(response -> {
                    AlertRule createdRule = response.getResponseBody();
                    Assertions.assertNotNull(createdRule);
                    assertThat(createdRule.getSymbol()).isEqualTo(dto.getSymbol());
                    assertThat(createdRule.getAlertCondition()).isEqualTo(dto.getAlertCondition());
                    assertThat(createdRule.getThreshold()).isEqualTo(dto.getThreshold());
                    assertThat(createdRule.isActive()).isTrue();
                    assertThat(createdRule.getId()).isNotNull();
                    assertThat(createdRule.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void getAllAlertRules_returnsRules(){
        AlertRuleRequestDto dto = new AlertRuleRequestDto();
        dto.setSymbol("SOLUSDT");
        dto.setAlertCondition(AlertCondition.ABOVE);
        dto.setThreshold(50.0);

        webTestClient.post()
                .uri("http://localhost:" + port + "/api/alerts")
                .body(Mono.just(dto), AlertRuleRequestDto.class)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("http://localhost:"+port+"/api/alerts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AlertRule.class)
                .consumeWith(response -> {
                    List<AlertRule> alertRules = response.getResponseBody();
                    Assertions.assertNotNull(alertRules);
                    assertThat(alertRules.size()).isGreaterThan(0);
                });
    }

    @Test
    void deleteAlertRule_removesRule(){
        AlertRuleRequestDto dto = new AlertRuleRequestDto();
        dto.setSymbol("ETHUSDT");
        dto.setAlertCondition(AlertCondition.BELOW);
        dto.setThreshold(2000.0);

        AlertRule created = webTestClient.post()
                .uri("http://localhost:" + port + "/api/alerts")
                .body(Mono.just(dto), AlertRuleRequestDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AlertRule.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(created);
        String id = created.getId();

        webTestClient.delete()
                .uri("http://localhost:" + port + "/api/alerts/" + id)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("http://localhost:" + port + "/api/alerts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AlertRule.class)
                .consumeWith(response -> {
                    List<AlertRule> rules = response.getResponseBody();
                    Assertions.assertNotNull(rules);

                    assertThat(rules)
                            .extracting(AlertRule::getId)
                            .doesNotContain(id);
                });
    }
}
