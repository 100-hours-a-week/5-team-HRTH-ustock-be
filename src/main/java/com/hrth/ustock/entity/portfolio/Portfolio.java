package com.hrth.ustock.entity.portfolio;

import com.hrth.ustock.dto.portfolio.PortfolioEmbedDto;
import com.hrth.ustock.dto.portfolio.PortfolioUpdateDto;
import com.hrth.ustock.entity.User;
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
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pf_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 150)
    private String name;

    private Long budget;

    private Long principal;

    private Long ret;

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<Holding> holdings = new ArrayList<>();

    public PortfolioEmbedDto toPortfolioDto() {
        PortfolioEmbedDto dto = PortfolioEmbedDto.builder()
                .id(id)
                .name(name)
                .budget(budget)
                .ror((double) ret / principal * 100)
                .build();
        dto.setRor(dto.getRor().isNaN() ? 0.0 : dto.getRor());
        return dto;
    }

    public void updatePortfolio(PortfolioUpdateDto portfolioUpdateDto) {
        this.budget = portfolioUpdateDto.getBudget();
        this.ret = portfolioUpdateDto.getRet();
        this.principal = portfolioUpdateDto.getPrincipal();
    }
}
