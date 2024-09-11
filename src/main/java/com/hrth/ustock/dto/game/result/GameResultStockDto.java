package com.hrth.ustock.dto.game.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultStockDto {
    private long stockId;
    private String fakeName;
    private String realName;
    List<GameYearlyResultDto> yearlyResults;
}
