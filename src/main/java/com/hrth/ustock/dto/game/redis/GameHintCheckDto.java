package com.hrth.ustock.dto.game.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHintCheckDto {
    long levelOneId;
    long levelTwoId;
    long levelThreeId;
}
