package com.sonkim.bookmarking.domain.user.repository;

import com.sonkim.bookmarking.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
