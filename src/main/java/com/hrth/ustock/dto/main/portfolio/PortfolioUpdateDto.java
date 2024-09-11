package com.hrth.ustock.dto.main.portfolio;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioUpdateDto {
    private long budget;
    private long principal;
    private long profit;
}
