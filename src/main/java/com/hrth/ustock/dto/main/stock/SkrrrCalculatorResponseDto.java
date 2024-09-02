package com.hrth.ustock.dto.main.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkrrrCalculatorResponseDto {
    private long price;
    private String candy;
    private String soul;
    private String chicken;
    private String iphone;
    private String slave;
}
