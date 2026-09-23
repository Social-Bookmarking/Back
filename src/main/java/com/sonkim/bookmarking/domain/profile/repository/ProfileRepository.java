package com.sonkim.bookmarking.domain.profile.repository;

import com.sonkim.bookmarking.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from Profile profile where profile.id = :id")
    Optional<Profile> findByIdForImageUpdate(Long id);
    Boolean existsByNickname(String nickname);
}
