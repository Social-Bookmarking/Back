package com.sonkim.bookmarking.domain.token.repository;

import com.sonkim.bookmarking.domain.token.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByAccountId(Long accountId);

    void deleteTokenByAccountId(Long accountId);

    void deleteByRefreshToken(String refreshToken);
}
