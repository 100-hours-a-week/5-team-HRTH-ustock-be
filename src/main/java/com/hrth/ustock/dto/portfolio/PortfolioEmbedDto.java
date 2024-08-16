package com.hrth.ustock.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioEmbedDto {
    private Long id = 0L;
    private String name = "";
    private Long budget = 0L;
    private Double ror = 0.0;
}
