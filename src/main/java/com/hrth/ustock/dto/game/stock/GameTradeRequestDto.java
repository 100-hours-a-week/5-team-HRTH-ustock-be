package com.hrth.ustock.dto.game.stock;

import com.hrth.ustock.entity.game.GameActing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameTradeRequestDto {
    private long stockId;
    private int quantity;
    private GameActing acting;
}
