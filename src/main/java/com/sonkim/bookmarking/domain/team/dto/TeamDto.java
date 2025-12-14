package com.sonkim.bookmarking.domain.team.dto;

import com.sonkim.bookmarking.domain.team.enums.TeamStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class TeamDto {

    // 그룹 생성 및 정보 수정
    @Data
    public static class TeamRequestDto {
        @Size(max = 20, message = "그룹 이름은 20자를 초과할 수 없습니다.")
        private String name;

        @Size(max = 200, message = "그룹 설명은 200자를 초과할 수 없습니다.")
        private String description;
    }

    // 그룹 정보 조회
    @Data
    @Builder
    public static class TeamResponseDto {
        private String name;
        private String description;
        private Long ownerId;
        private String ownerName;
        private TeamStatus status;
        private LocalDateTime deletionScheduledAt;
    }

    // 그룹 정보 조회(owner 제외)
    @Data
    @Builder
    public static class MyTeamDto {
        private Long teamId;
        private String groupName;
        private String description;
    }

    // 그룹 가입 요청
    @Data
    public static class JoinRequestDto {
        @NotBlank(message = "초대 코드는 필수입니다.")
        private String inviteCode;
    }

    // 그룹 생성 답변
    @Data
    @Builder
    public static class CreateResponseDto {
        private Long teamId;
    }

    // 초대 코드 재생성 답변
    @Data
    @Builder
    public static class CodeResponseDto {
        private String code;
    }

    // 그룹 소유주 이전
    @Data
    public static class OwnerTransferDto {
        @NotBlank(message = "새로운 소유주 ID는 필수입니다.")
        private Long newOwnerId;
    }
}
