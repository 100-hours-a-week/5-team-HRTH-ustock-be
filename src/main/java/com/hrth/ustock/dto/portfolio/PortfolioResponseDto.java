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
    private String name = "";
    private Long budget = 0L;
    private Long principal = 0L;
    private Long ret = 0L;
    private Double ror = 0.0;
    private List<HoldingEmbedDto> stocks = new ArrayList<>();
}
