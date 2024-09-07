package com.hrth.ustock.dto.game.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameNewsResponseDto {
    private String date;
    private String title;
    private String url;
    private String publisher;
}
