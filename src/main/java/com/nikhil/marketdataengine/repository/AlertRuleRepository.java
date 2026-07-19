package com.nikhil.marketdataengine.repository;

import com.nikhil.marketdataengine.model.AlertRule;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AlertRuleRepository extends ReactiveMongoRepository<AlertRule, String> {
    public Flux<AlertRule> findBySymbolAndActiveTrue(String symbol);
}
