package com.hrth.ustock.dto.news;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsRequestDto {
    private int code = 0;
    private int period = 0;
}
