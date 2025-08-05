package com.sonkim.bookmarking.domain.profile.service;

import com.sonkim.bookmarking.domain.profile.entity.Profile;
import com.sonkim.bookmarking.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public Profile createProfile(String nickname) {
        Profile profile = Profile.builder()
                .nickname(nickname)
                .build();

        return profileRepository.save(profile);
    }

}
