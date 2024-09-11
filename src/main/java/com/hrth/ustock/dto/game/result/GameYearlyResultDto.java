package com.hrth.ustock.dto.game.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameYearlyResultDto {
    private int year;
    private int price;
    private GameResultNewsDto news;
}
