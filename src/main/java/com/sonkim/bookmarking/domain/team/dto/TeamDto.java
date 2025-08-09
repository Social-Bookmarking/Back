package com.sonkim.bookmarking.domain.team.dto;

import lombok.Builder;
import lombok.Data;

public class TeamDto {

    // 그룹 생성 및 정보 수정
    @Data
    public static class RequestDto {
        private String name;
        private String description;
    }

    // 그룹 정보 조회
    @Data
    @Builder
    public static class ResponseDto {
        private String name;
        private String description;
        private Long ownerId;
        private String ownerName;
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
        private String inviteCode;
    }
}
