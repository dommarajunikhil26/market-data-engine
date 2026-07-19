package com.nikhil.marketdataengine.dto;

import com.nikhil.marketdataengine.model.AlertCondition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleRequestDto {
    @NotNull
    private String symbol;
    @NotNull
    private AlertCondition alertCondition;
    @NotNull
    private double threshold;
}
