package com.hrth.ustock.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartDTO {
    private Integer open;
    private Integer close;
    private Integer high;
    private Integer low;
}
