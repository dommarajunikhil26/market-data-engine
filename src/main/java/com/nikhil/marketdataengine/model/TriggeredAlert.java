package com.nikhil.marketdataengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "triggered_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggeredAlert {
    @Id
    private String id;
    @Indexed
    private String alertRuleId;
    private String symbol;
    private double price;
    @CreatedDate
    private Instant triggeredAt;
}
