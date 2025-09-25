package com.sonkim.bookmarking.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sonkim.bookmarking.domain.user.entity.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.sonkim.bookmarking.domain.user.entity.QUser.user;
import static com.sonkim.bookmarking.domain.profile.entity.QProfile.profile;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<User> findByIdWithProfile(Long userId) {
        User result = queryFactory
                .selectFrom(user)
                .join(user.profile, profile).fetchJoin()
                .where(user.id.eq(userId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
