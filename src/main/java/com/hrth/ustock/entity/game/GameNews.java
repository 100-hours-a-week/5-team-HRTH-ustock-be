package com.hrth.ustock.entity.game;

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
@Table(name = "game_news")
public class GameNews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    private String date;

    private String title;

    private String url;

    private String publisher;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private GameStockInfo gameStockInfo;
}
