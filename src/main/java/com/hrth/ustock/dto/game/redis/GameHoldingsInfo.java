package com.hrth.ustock.dto.game.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameHoldingsInfo {
    private String stockName;
    private long average;
    private int price;
    private double ror;
    private int quantity;
    private long stockId;
}
