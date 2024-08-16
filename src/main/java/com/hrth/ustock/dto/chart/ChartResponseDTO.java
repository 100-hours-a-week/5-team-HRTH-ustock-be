package com.hrth.ustock.dto.chart;

import com.hrth.ustock.dto.news.NewsEmbedDto;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartResponseDTO {
    private Long time;
    // 나중에 물어보기
    private List<ChartDTO> candle;
    private List<NewsEmbedDto> news;
}
