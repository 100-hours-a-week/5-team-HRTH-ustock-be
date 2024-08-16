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
public class Hint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hint_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private GameStock stock;

    private int year;

    @Column(columnDefinition = "TEXT")
    private String hint;

    private int level;
}
