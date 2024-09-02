package com.hrth.ustock.dto.main.holding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingRequestDto {
    private int quantity;
    private long price;
}
