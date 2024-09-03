package com.hrth.ustock.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHoldingDto {
    private String stockName;
    private int quantity;
    private int purchasePrice;
    private int currentPrice;
    private double ret;
}
