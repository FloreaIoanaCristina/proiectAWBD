package com.unibuc.management.services;

import com.unibuc.management.domain.User;
import com.unibuc.management.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Spring Security solicită încărcarea datelor pentru utilizatorul: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Autentificare eșuată: Utilizatorul cu username-ul '{}' nu a fost găsit în baza de date.", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.info("Utilizatorul '{}' a fost găsit cu succes în DB. Se încarcă rolul: [{}]", user.getUsername(), user.getRole().name());

        // Built-in User principal + SimpleGrantedAuthority so the session serialized
        // to Redis can be deserialized by every service (no custom classes needed).
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getAuthority()))
        );
    }
}
