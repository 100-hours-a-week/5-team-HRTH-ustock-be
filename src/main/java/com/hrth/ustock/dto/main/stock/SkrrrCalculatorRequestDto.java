package com.hrth.ustock.dto.main.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkrrrCalculatorRequestDto {
    @Schema(
            description = "사용가능한 총 금액",
            minimum = "1",
            maximum = "9999999999999"
    )
    private long price;
    @Schema(
            description = "과거 날짜",
            minimum = "2014/01/01",
            maximum = "$Current date"
    )
    private String date;
}
