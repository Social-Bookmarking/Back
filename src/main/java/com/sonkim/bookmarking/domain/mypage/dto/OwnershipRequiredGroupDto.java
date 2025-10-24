package com.sonkim.bookmarking.domain.mypage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnershipRequiredGroupDto {
    private Long groupId;
    private String groupName;
}