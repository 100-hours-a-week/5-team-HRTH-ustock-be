package com.hrth.ustock.dto.game.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAiStockDto {
    private long id;
    private String name;
    private int quantity;
}
