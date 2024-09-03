package com.hrth.ustock.entity.game;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "game_stock_info")
public class GameStockInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long id;

    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "stock_name")
    private String stockName;

    private int price;

    private String year;

    @OneToMany(mappedBy = "gameStockInfo")
    private List<GameHint> gameHints;
}
