package com.hrth.ustock.entity.game;

import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "game_hint")
public class GameHint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hint_id")
    private Long id;

    private String hint;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ONE', 'TWO', 'THREE')")
    private HintLevel level;

    @ManyToOne
    @JoinColumn(name = "yearly_id")
    private GameStockYearly gameStockYearly;

    public GameHintResponseDto toDto() {
        return GameHintResponseDto.builder()
                .hint(this.hint)
                .build();
    }
}
