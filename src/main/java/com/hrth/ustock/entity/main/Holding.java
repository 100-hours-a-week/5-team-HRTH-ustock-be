package com.hrth.ustock.entity.main;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int quantity;

    private long average;

    public void additionalBuyHolding(int quantity, long price) {
        long total = this.quantity * this.average;
        total += quantity * price;
        this.quantity += quantity;
        this.average = total / this.quantity;
    }

    public void updateHolding(int quantity, long price) {
        this.quantity = quantity;
        this.average = price;
    }
}
