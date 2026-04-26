package com.sa.authservice.faker;

import com.sa.authservice.model.entity.User;
import com.sa.authservice.model.enums.Role;
import com.sa.authservice.repository.RoleRepository;
import com.sa.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        seedUser("customer1", "customer1@example.com", "Customer One", Role.CUSTOMER);
        seedUser("organizer1", "organizer1@example.com", "Organizer One", Role.ORGANIZER);
        seedUser("staff1", "staff1@example.com", "Staff One", Role.STAFF);
    }

    private void seedUser(String username, String email, String fullName, Role roleEnum) {
        if (userRepository.findByUsername(username).isPresent()) return;
        roleRepository.findById(roleEnum.name()).ifPresent(role -> {
            userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .fullName(fullName)
                    .password(passwordEncoder.encode("password"))
                    .roles(new java.util.HashSet<>(java.util.Set.of(role)))
                    .enabled(true)
                    .build());
        });
    }
}
