package com.sonkim.bookmarking.domain.token.entity;

import com.sonkim.bookmarking.domain.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 ID
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // 리프레시 토큰
    @Column(nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    // 만료 시간
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public void updateToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }
}
