package com.sonkim.bookmarking.auth.service;

import com.sonkim.bookmarking.domain.user.entity.User;
import com.sonkim.bookmarking.domain.user.repository.UserRepository;
import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다: " + username));

        return new UserDetailsImpl(user.getId(), user.getUsername(), user.getPassword());
    }
}
