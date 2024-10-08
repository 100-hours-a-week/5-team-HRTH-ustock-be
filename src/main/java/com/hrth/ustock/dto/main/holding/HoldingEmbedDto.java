package com.hrth.ustock.dto.main.holding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingEmbedDto {
    private String code;
    private String name;
    private String logo;
    private int quantity;
    private long average;
    private double profitRate;
}
