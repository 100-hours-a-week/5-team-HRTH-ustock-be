package com.hrth.ustock.dto.main.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketResponseDto {
    private double price;
    private double change;
    private double changeRate;
}
