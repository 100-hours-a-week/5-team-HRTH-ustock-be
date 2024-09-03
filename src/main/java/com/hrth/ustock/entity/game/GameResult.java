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
@Table(name = "game_result")
public class GameResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    private long budget;

    @Column(name = "player_type")
    @Enumerated(EnumType.STRING)
    private PlayerType playerType;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameInfo gameInfo;
}
