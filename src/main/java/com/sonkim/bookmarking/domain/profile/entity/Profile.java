package com.sonkim.bookmarking.domain.profile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 닉네임
    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    // 프로필 이미지
    @Column(columnDefinition = "TEXT")
    private String imageKey;

    // 정보 수정일
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 탈퇴 처리
    public void anonymize() {
        this.nickname = "탈퇴한 사용자";
        this.imageKey = null;
    }

    // 이미지 업데이트
    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    // 닉네임 업데이트
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
