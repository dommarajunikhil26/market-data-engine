package com.nikhil.marketdataengine.controller;

import com.nikhil.marketdataengine.dto.AlertRuleRequestDto;
import com.nikhil.marketdataengine.model.AlertRule;
import com.nikhil.marketdataengine.service.AlertRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/alerts")
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    public AlertRuleController(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    @PostMapping
    public Mono<ResponseEntity<AlertRule>> createAlertRule(@RequestBody @Valid AlertRuleRequestDto dto) {
        return alertRuleService.createAlertRule(dto);
    }

    @GetMapping
    public Flux<AlertRule> getAllAlertRules() {
        return alertRuleService.getAllAlertRules();
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAlertRule(@PathVariable String id) {
        return alertRuleService.deleteAlertRule(id);
    }

}
