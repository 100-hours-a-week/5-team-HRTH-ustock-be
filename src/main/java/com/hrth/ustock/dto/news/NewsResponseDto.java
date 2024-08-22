package com.hrth.ustock.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsResponseDto {
    private String name;
    private String code;
    private String title;
    private String date;
    private String url;
    private String publisher;
}
