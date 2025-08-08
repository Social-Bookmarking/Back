package com.sonkim.bookmarking.domain.team.dto;

import com.sonkim.bookmarking.domain.team.enums.Permission;
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
        private Permission permission;
    }
}
