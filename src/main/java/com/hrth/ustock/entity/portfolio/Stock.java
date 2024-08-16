package com.hrth.ustock.entity.portfolio;

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
    @Column(length = 6, name = "stock_code")
    private String code;

    @Column(length = 150, name = "stock_name")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String logo;

}
