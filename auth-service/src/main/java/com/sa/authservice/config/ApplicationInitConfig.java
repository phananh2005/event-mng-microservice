package com.sa.authservice.config;

import com.sa.authservice.model.entity.Permission;
import com.sa.authservice.model.entity.Role;
import com.sa.authservice.model.entity.User;
import com.sa.authservice.repository.PermissionRepository;
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

import java.util.HashSet;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @Bean
    @ConditionalOnProperty(prefix = "spring", value = "datasource.driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            Permission manageEvents = ensurePermission("MANAGE_EVENTS", "Create, update, and delete events");
            Permission manageUsers = ensurePermission("MANAGE_USERS", "Manage system users");
            Permission viewDashboard = ensurePermission("VIEW_DASHBOARD", "View analytical dashboard");

            Role adminRole = ensureRole("ADMIN", "System Administrator", List.of(manageEvents, manageUsers, viewDashboard));
            ensureRole("ORGANIZER", "Event Organizer", List.of(manageEvents));
            ensureRole("CUSTOMER", "Regular Customer", List.of());
            ensureRole("STAFF", "Event Staff", List.of());

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .fullName("Administrator")
                        .email("admin@gmail.com")
                        .phone("0123456789")
                        .roles(new HashSet<>(List.of(adminRole)))
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.warn("Default admin created — username: admin, password: admin. Change it immediately!");
            } else {
                log.info("Admin account already exists, skipping init.");
            }
        };
    }

    private Permission ensurePermission(String name, String description) {
        return permissionRepository.findById(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).description(description).build()));
    }

    private Role ensureRole(String roleName, String description, List<Permission> permissions) {
        return roleRepository.findById(roleName)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(roleName)
                            .description(description)
                            .permissions(new HashSet<>(permissions))
                            .build();
                    return roleRepository.save(role);
                });
    }
}
