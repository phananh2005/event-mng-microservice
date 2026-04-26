package com.sa.authservice.config;

import com.sa.authservice.model.entity.Role;
import com.sa.authservice.model.entity.User;
import com.sa.authservice.repository.RoleRepository;
import com.sa.authservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Bean
    @ConditionalOnProperty(prefix = "spring", value = "datasource.driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            Role adminRole = ensureRole(com.sa.authservice.model.enums.Role.ADMIN);
            ensureRole(com.sa.authservice.model.enums.Role.ORGANIZER);
            ensureRole(com.sa.authservice.model.enums.Role.CUSTOMER);
            ensureRole(com.sa.authservice.model.enums.Role.STAFF);

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .fullName("Administrator")
                        .email("admin@gmail.com")
                        .phone("0123456789")
                        .roles(new ArrayList<>(List.of(adminRole)))
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.warn("Default admin created — username: admin, password: admin. Change it immediately!");
            } else {
                log.info("Admin account already exists, skipping init.");
            }
        };
    }

    private Role ensureRole(com.sa.authservice.model.enums.Role roleEnum) {
        return roleRepository.findByName(roleEnum)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleEnum).build()));
    }
}
