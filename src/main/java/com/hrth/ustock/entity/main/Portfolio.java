package com.hrth.ustock.entity.main;

import com.hrth.ustock.dto.main.portfolio.PortfolioEmbedDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// TODO: develop -> main에 push 할 때 portfolio_temp를 삭제 후 push 해야함
@Table(name = "portfolio_temp")
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pf_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    private Long budget;

    private Long principal;

    private Long profit;

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.EAGER)
    private List<Holding> holdings;

    public PortfolioEmbedDto toPortfolioDto() {
        return PortfolioEmbedDto.builder()
                .id(this.id)
                .name(this.name)
                .budget(this.budget)
                .profitRate((this.principal == 0L) ? 0.0 : (double) this.profit / this.principal * 100)
                .build();
    }

    public void updatePortfolio(PortfolioUpdateDto portfolioUpdateDto) {
        this.budget = portfolioUpdateDto.getBudget();
        this.profit = portfolioUpdateDto.getProfit();
        this.principal = portfolioUpdateDto.getPrincipal();
    }
}
