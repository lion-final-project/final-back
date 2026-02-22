package com.example.finalproject.global.security;

import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //사용자 조회
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE || user.getDeletedAt() != null) {
            throw new UsernameNotFoundException("비활성화된 사용자입니다.");
        }

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .toList();
        return new CustomUserDetails(user, roles);
    }
}
