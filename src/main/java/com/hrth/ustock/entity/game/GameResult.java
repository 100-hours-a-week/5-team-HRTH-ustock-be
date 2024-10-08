package com.hrth.ustock.entity.game;

import com.hrth.ustock.dto.game.result.GameRankingDto;
import com.hrth.ustock.entity.main.User;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public GameRankingDto toDto() {
        final long START_BUDGET = 500_000L;

        return GameRankingDto.builder()
                .userId(user.getUserId())
                .nickname(nickname)
                .total(budget)
                .profitRate((double) (budget - START_BUDGET) / START_BUDGET * 100)
                .build();
    }
}
