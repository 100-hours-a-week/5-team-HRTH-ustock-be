package com.hrth.ustock.dto.game.user;

import com.hrth.ustock.dto.game.redis.GameHoldingsInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameUserResponseDto {
    // TODO: 디버깅이 종료되면 year 삭제
    private int year;
    private String nickname;
    private long total;
    private long budget;
    private long changeFromLast;
    private double changeRateFromLast;
    private long changeFromStart;
    private double changeRateFromStart;
    private List<GameHoldingsInfoDto> holdingList;
}
