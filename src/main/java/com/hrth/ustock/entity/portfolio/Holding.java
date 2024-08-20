package com.hrth.ustock.entity.portfolio;

import com.hrth.ustock.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private long quantity;

    private long average;

    public void additionalBuyHolding(long quantity, long price) {
        long total = this.quantity * this.average;
        total += quantity * price;
        this.quantity += quantity;
        this.average = total / this.quantity;
    }

    public void updateHolding(long quantity, long price) {
        this.quantity = quantity;
        this.average = price;
    }
}
