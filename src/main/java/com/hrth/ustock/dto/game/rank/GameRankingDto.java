package com.hrth.ustock.dto.game.rank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRankingDto {
    private String nickname;
    private long budget;
    private double ror;
}
