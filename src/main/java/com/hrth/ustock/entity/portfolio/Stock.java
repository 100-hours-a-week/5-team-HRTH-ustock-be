package com.hrth.ustock.entity.portfolio;

import com.hrth.ustock.dto.stock.StockDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "stocks")
public class Stock {
    @Id
    @Column(name = "stock_code")
    private String code;

    @Column(name = "stock_name")
    private String name;

    private String logo;

    public StockDto toDTO() {
        return StockDto.builder()
                .code(this.code)
                .name(this.name)
                .logo(this.logo)
                .build();
    }
}
