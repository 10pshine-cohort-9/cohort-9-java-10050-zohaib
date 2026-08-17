package com.contactmanagement.security;

import com.contactmanagement.model.User;
import com.contactmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = resolveUser(identifier);

        log.debug("Loaded UserDetails for identifier '{}'", identifier);

        String principal = (user.getEmail() != null) ? user.getEmail() : user.getPhone();

        return org.springframework.security.core.userdetails.User.builder()
                .username(principal)
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }

    private User resolveUser(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "No user found with email: " + identifier));
        }
        return userRepository.findByPhone(identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with phone: " + identifier));
    }
}
