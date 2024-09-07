package com.hrth.ustock.dto.game.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStocksRedisDto {
    private Long id;
    private String stockName;
}
