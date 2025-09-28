package com.sonkim.bookmarking.domain.team.entity;

import com.sonkim.bookmarking.domain.team.dto.TeamDto;
import com.sonkim.bookmarking.domain.team.enums.TeamStatus;
import com.sonkim.bookmarking.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    // 그룹 상태
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TeamStatus status = TeamStatus.ACTIVE;

    // 삭제 예정일
    private LocalDateTime deletionScheduledAt;

    public void update(TeamDto.TeamRequestDto teamRequestDto) {
        if (teamRequestDto.getName() != null) this.name = teamRequestDto.getName();
        if (teamRequestDto.getDescription() != null) this.description = teamRequestDto.getDescription();
    }

    public void updateCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    // 삭제 대기 상태로 변경
    public void scheduleDeletion() {
        this.status = TeamStatus.PENDING_DELETION;
        LocalDate deletionDate = LocalDate.now().plusDays(8);
        this.deletionScheduledAt = deletionDate.atTime(4, 0);
    }

    // 삭제 취소 후 다시 활성 상태로 변경
    public void cancelDeletion() {
        this.status = TeamStatus.ACTIVE;
        this.deletionScheduledAt = null;
    }
}
