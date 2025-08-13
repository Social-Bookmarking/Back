package com.sonkim.bookmarking.domain.team.entity;

import com.sonkim.bookmarking.domain.team.dto.TeamDto;
import com.sonkim.bookmarking.domain.team.enums.TeamStatus;
import com.sonkim.bookmarking.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유주 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private User user;

    // 그룹 이름
    @Column(nullable = false, length = 20)
    private String name;

    // 그룹 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 초대 코드
    @Column(length = 20, unique = true)
    private String inviteCode;

    // 생성일
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TeamStatus status = TeamStatus.ACTIVE;

    private LocalDateTime deletionScheduledAt;

    public void update(TeamDto.RequestDto requestDto) {
        if (requestDto.getName() != null) this.name = requestDto.getName();
        if (requestDto.getDescription() != null) this.description = requestDto.getDescription();
    }

    public void updateCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    // 삭제 대기 상태로 변경
    public void scheduleDeletion() {
        this.status = TeamStatus.PENDING_DELETION;
        this.deletionScheduledAt = LocalDateTime.now().plusDays(7);
    }

    // 삭제 취소 후 다시 활성 상태로 변경
    public void cancelDeletion() {
        this.status = TeamStatus.ACTIVE;
        this.deletionScheduledAt = null;
    }
}
