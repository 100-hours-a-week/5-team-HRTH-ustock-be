package com.hrth.ustock.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockResponseDTO {
    private String code;
    private String name;
    private int price;
    private int change;
    private double changeRate;
}
