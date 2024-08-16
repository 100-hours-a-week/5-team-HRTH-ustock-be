package com.hrth.ustock.entity;

import com.hrth.ustock.entity.portfolio.Portfolio;
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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50, unique = true)
    private String nickname;

    @Column(length = 20)
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(columnDefinition = "TEXT")
    private String profile_image;

    @OneToMany(mappedBy = "user")
    private List<Portfolio> portfolios;
}
