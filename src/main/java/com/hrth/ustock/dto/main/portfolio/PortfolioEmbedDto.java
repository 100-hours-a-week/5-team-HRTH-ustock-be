package com.hrth.ustock.dto.main.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioEmbedDto {
    private long id;
    private String name;
    private long budget;
    private double profitRate;
}
