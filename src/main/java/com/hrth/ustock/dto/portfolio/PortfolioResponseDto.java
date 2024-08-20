package com.hrth.ustock.dto.portfolio;

import com.hrth.ustock.dto.holding.HoldingEmbedDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponseDto {
    private String name;
    private long budget;
    private long principal;
    private long ret;
    private double ror;
    private List<HoldingEmbedDto> stocks = new ArrayList<>();
}
