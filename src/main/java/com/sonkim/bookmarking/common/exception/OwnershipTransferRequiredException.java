package com.sonkim.bookmarking.common.exception;

import com.sonkim.bookmarking.domain.mypage.dto.OwnershipRequiredGroupDto;
import lombok.Getter;

import java.util.List;

public class OwnershipTransferRequiredException extends RuntimeException {
    @Getter
    private final List<OwnershipRequiredGroupDto> groups;

    public OwnershipTransferRequiredException(List<OwnershipRequiredGroupDto> groups) {
        super("소유권 이전이 필요한 그룹이 존재하여 탈퇴할 수 없습니다.");
        this.groups = groups;
    }
}
