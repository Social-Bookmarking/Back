package com.sonkim.bookmarking.domain.group.entity;

import com.sonkim.bookmarking.domain.account.entity.Account;
import com.sonkim.bookmarking.domain.group.enums.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // 그룹 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 권한
    @Column(nullable = false)
    private Permission permission;

    // 그룹 가입일
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}
