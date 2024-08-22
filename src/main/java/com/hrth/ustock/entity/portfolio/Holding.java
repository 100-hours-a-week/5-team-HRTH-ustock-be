package com.hrth.ustock.entity.portfolio;

import com.hrth.ustock.entity.User;
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

    private int average;

    public void additionalBuyHolding(int quantity, int price) {
        long total = (long) this.quantity * this.average;
        total += (long) quantity * price;
        this.quantity += quantity;
        this.average = (int)(total / this.quantity);
    }

    public void updateHolding(int quantity, int price) {
        this.quantity = quantity;
        this.average = price;
    }
}
