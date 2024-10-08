package com.hrth.ustock.dto.game.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultNewsDto {
    private String title;
    private String url;
    private String publisher;
    private String date;
}
