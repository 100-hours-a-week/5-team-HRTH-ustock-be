package com.hrth.ustock.dto.main.portfolio;

import com.hrth.ustock.dto.main.holding.HoldingEmbedDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponseDto {
    private String name;
    private long budget;
    private long principal;
    private long profit;
    private double profitRate;
    private List<HoldingEmbedDto> stocks = new ArrayList<>();
}
