package com.nikhil.marketdataengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "alert_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule {
    @Id
    private String id;
    private String symbol;
    private AlertCondition alertCondition;
    private double threshold;
    private boolean active;
    @CreatedDate
    private Instant createdAt;
}
