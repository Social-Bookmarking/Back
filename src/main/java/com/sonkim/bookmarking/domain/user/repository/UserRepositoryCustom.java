package com.sonkim.bookmarking.domain.user.repository;

import com.sonkim.bookmarking.domain.user.entity.User;

import java.util.Optional;

public interface UserRepositoryCustom {
    // 사용자 프로필 조회를 위해 프로필 정보까지 한 번에 가져옴
    Optional<User> findByIdWithProfile(Long userId);
}
