package com.hrth.ustock.dto.holding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingEmbedDto {
    private String code;
    private String name;
    private int quantity;
    private int average;
    private double ror;
}
