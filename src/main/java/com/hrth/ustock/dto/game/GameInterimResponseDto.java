package com.hrth.ustock.dto.game;

import com.hrth.ustock.entity.game.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInterimResponseDto {
    private long budget;
    private long changeFromLast;
    private double changeRateFromLast;
    private long changeFromStart;
    private double changeRateFromStart;
    private PlayerType playerType;
    private List<GameHoldingDto> holdingList;
}
