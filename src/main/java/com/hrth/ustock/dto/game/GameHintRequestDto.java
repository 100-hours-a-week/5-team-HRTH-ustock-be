package com.hrth.ustock.dto.game;

import com.hrth.ustock.entity.game.HintLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHintRequestDto {
    private long stockId;
    private int year;
    private HintLevel level;
}
