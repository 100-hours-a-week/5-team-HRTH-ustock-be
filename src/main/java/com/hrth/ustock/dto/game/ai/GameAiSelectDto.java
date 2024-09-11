package com.hrth.ustock.dto.game.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAiSelectDto {
    private List<GameAiStockDto> sell;
    private List<GameAiStockDto> buy;
    private String reason;
}
