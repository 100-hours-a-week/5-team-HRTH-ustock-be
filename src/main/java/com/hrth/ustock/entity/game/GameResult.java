package com.hrth.ustock.entity.game;

import com.hrth.ustock.dto.game.result.GameRankingDto;
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

    private String nickname;

    public GameRankingDto toDto() {
        final long START_BUDGET = 500_000L;

        return GameRankingDto.builder()
                .nickname(nickname)
                .budget(budget)
                .profitRate((double) (budget - START_BUDGET) / START_BUDGET * 100)
                .build();
    }
}
