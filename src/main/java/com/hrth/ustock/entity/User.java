package com.hrth.ustock.entity;

import com.hrth.ustock.dto.oauth2.UserOauthDto;
import com.hrth.ustock.entity.portfolio.Holding;
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

    private String nickname;

    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "provider_image")
    private String providerImage;

    private String role;

    @OneToMany(mappedBy = "user")
    private List<Portfolio> portfolios;

    @OneToMany(mappedBy = "user")
    private List<Holding> holdings;

    public void updateUser(String nickname, String profileImage, String role) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role;
    }

    public UserOauthDto toOAuthDto() {
        return UserOauthDto.builder()
                .userId(this.userId)
                .provider(this.provider)
                .providerId(this.providerId)
                .providerName(this.providerName)
                .profile(this.providerImage)
                .role(this.role)
                .build();
    }

    public void updateUserOAuth(UserOauthDto userOauthDto) {
        this.providerImage = userOauthDto.getProfile();
        this.providerName = userOauthDto.getProviderName();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void addPortfolio(Portfolio portfolio) {
        this.portfolios.add(portfolio);
    }
}
