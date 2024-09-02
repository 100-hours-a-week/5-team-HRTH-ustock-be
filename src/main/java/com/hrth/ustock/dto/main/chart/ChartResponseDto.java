package com.hrth.ustock.dto.main.chart;

import com.hrth.ustock.dto.main.news.NewsEmbedDto;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartResponseDto {
    private String date;
    private ChartDto candle;
    private List<NewsEmbedDto> news;
}
