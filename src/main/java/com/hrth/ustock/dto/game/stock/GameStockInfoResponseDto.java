package com.hrth.ustock.dto.game.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStockInfoResponseDto {
    private long stockId;
    private String name;
    private int current;
    private int prev;
    private int change;
    private Double changeRate;
}