package com.hrth.ustock.dto.portfolio;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioUpdateDto {
    private Long budget = 0L;
    private Long principal = 0L;
    private Long ret = 0L;
}
