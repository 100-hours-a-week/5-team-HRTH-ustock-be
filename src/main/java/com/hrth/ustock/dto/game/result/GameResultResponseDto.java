package com.hrth.ustock.dto.game.result;

import com.hrth.ustock.entity.game.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultResponseDto {
    // TODO: 디버깅 끝나면 year 삭제
    private int year;
    private String nickname;
    private long total;
    private double profitRate;
    private PlayerType playerType;
}
