package com.sonkim.bookmarking.domain.team.dto;

import com.sonkim.bookmarking.domain.team.enums.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public class TeamMemberDto {

    // 멤버 목록 조회용 DTO
    @Data
    @Builder
    public static class MemberResponseDto {
        private Long userId;
        private String name;
        private String profileImageUrl;
        private Permission permission;
    }

    // 멤버 역할 수정용 DTO
    @Data
    public static class UpdatePermissionRequestDto {
        @NotNull(message = "멤버 역할은 필수입니다.")
        private Permission permission;
    }

    // 멤버 역할 조회 응답용 DTO
    @Data
    @Builder
    public static class GetPermissionResponseDto {
        private Permission permission;
    }
}
