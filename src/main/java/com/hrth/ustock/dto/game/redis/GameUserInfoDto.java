package com.hrth.ustock.dto.game.redis;

import com.hrth.ustock.entity.game.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameUserInfoDto {
    private PlayerType playerType;
    private String nickname;
    private long budget;
    private long prev;
    private List<GameHoldingsInfoDto> holdings;
    private GameHintCheckDto hintCheck;
}