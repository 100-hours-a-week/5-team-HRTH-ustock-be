package com.hrth.ustock.entity;

import com.hrth.ustock.dto.oauth2.UserOauthDTO;
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

    @Column(length = 50, name = "provider_name")
    private String providerName;

    @Column(columnDefinition = "TEXT", name = "profile_image")
    private String profileImage;

    @Column(columnDefinition = "TEXT", name = "provider_image")
    private String providerImage;

    @Column(length = 20)
    private String role;

    @OneToMany(mappedBy = "user")
    private List<Portfolio> portfolios;

    public void updateUser(String nickname, String profileImage, String role) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role;
    }

    public void updateUserOAuth(UserOauthDTO userOauthDTO) {
        this.providerImage = userOauthDTO.getPicture();
        this.providerName = userOauthDTO.getName();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void addPortfolio(Portfolio portfolio) {
        this.portfolios.add(portfolio);
    }
}
