package com.hrth.ustock.dto.main.holding;

import io.swagger.v3.oas.annotations.Parameter;
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
    @Parameter(
            name = "quantity",
            description = "매수할 수량, 1~99,999",
            schema = @Schema(
                    description = "매수할 수량",
                    minimum = "1",
                    maximum = "99999"
            )
    )
    private int quantity;
    @Parameter(
            name = "price",
            description = "매수할 가격, 1~9,999,999,999",
            schema = @Schema(
                    description = "매수할 가격",
                    minimum = "1",
                    maximum = "9999999999"
            )
    )
    private long price;
}
