package com.hrth.ustock.dto.main.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllMarkterResponseDto {
    private MarketResponseDto kospi;
    private MarketResponseDto kosdaq;
}
