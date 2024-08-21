package com.hrth.ustock.dto.chart;

import com.hrth.ustock.dto.news.NewsEmbedDto;
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
