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
    private String code = "";
    private String name = "";
    private Integer quantity = 0;
    private Integer average = 0;
    private Double ror = 0.0;
}
