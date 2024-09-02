package com.hrth.ustock.dto.main.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartDto {
    private int open;
    private int close;
    private int high;
    private int low;
}
