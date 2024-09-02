package com.hrth.ustock.dto.main.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockListDto {
    private List<StockResponseDto> stocks = new ArrayList<>();
}
