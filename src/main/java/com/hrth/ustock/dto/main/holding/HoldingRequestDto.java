package com.hrth.ustock.dto.main.holding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingRequestDto {
    @Schema(
            description = "매수할 수량",
            minimum = "1",
            maximum = "99999"
    )
    private int quantity;
    @Schema(
            description = "매수할 가격",
            minimum = "1",
            maximum = "9999999999"
    )
    private long price;
}
