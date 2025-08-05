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
    private String imageUrl;

    // 정보 수정일
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

}
