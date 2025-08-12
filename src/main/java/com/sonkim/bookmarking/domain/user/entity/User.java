package com.sonkim.bookmarking.domain.user.entity;

import com.sonkim.bookmarking.domain.profile.entity.Profile;
import com.sonkim.bookmarking.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

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

    // 계정 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    private LocalDateTime deletedAt;

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // 탈퇴 처리
    public void withdraw() {
        this.userStatus = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.password = null;
        profile.anonymize();
    }
}
