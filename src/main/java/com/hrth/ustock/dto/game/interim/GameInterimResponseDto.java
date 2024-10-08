package com.hrth.ustock.dto.game.interim;

import com.hrth.ustock.dto.game.user.GameUserResponseDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
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
    int year;
    GameUserResponseDto userInfo;
    List<GameStockInfoResponseDto> stockList;
}
