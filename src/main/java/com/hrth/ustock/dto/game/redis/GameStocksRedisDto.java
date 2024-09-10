package com.hrth.ustock.dto.game.redis;

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
