package com.sonkim.bookmarking.domain.account.entity;

import com.sonkim.bookmarking.domain.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프로필 ID
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    // 아이디
    @Column(nullable = false, unique = true)
    private String username;

    // 비밀번호
    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    // 가입일
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
