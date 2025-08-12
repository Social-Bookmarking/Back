package com.sonkim.bookmarking.domain.user.repository;

import com.sonkim.bookmarking.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // 사용자 프로필 조회를 위해 프로필 정보까지 한 번에 가져옴
    @Query("SELECT u FROM User u JOIN FETCH u.profile WHERE u.id = :userId")
    Optional<User> findByIdWithProfile(@Param("userId") Long userId);
}
