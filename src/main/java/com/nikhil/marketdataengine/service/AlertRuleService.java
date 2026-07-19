package com.nikhil.marketdataengine.service;

import com.nikhil.marketdataengine.dto.AlertRuleRequestDto;
import com.nikhil.marketdataengine.model.AlertRule;
import com.nikhil.marketdataengine.repository.AlertRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;

    public AlertRuleService(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    public Mono<ResponseEntity<AlertRule>> createAlertRule(AlertRuleRequestDto dto) {
        AlertRule alertRule = new AlertRule();
        alertRule.setSymbol(dto.getSymbol());
        alertRule.setAlertCondition(dto.getAlertCondition());
        alertRule.setThreshold(dto.getThreshold());
        alertRule.setActive(true);
        alertRule.setCreatedAt(Instant.now());
        return alertRuleRepository.save(alertRule)
                .map(savedAt -> ResponseEntity.status(HttpStatus.CREATED).body(savedAt));
    }

    public Flux<AlertRule> getAllAlertRules() {
        return alertRuleRepository.findAll();
    }

    public Mono<ResponseEntity<Void>> deleteAlertRule(String id) {
        return alertRuleRepository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().build()));

    }
}
