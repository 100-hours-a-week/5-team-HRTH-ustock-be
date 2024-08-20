package com.hrth.ustock.entity.portfolio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "holdings")
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holdings_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pf_id")
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "stock_code")
    private Stock stock;

    private int quantity;

    private int average;

    public void additionalBuyHolding(int quantity, int price) {
        long total = (long) this.quantity * this.average;
        total += (long) quantity * price;
        this.quantity += quantity;
        this.average = (int) (total / this.quantity);
    }

    public void updateHolding(int quantity, int price) {
        this.quantity = quantity;
        this.average = price;
    }
}
