package com.sa.event_mng.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sa.event_mng.model.entity.Role;
import com.sa.event_mng.model.entity.User;
import com.sa.event_mng.repository.RoleRepository;
import com.sa.event_mng.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

  PasswordEncoder passwordEncoder;

  private final RoleRepository roleRepository;

  @Bean
  @ConditionalOnProperty(
      prefix = "spring",
      value = "datasource.driver-class-name",
      havingValue = "com.mysql.cj.jdbc.Driver")
  ApplicationRunner applicationRunner(UserRepository userRepo) {
    log.info("CONFIG: Init Application");
    return args -> {
      Role adminRole = ensureRole(com.sa.event_mng.model.enums.Role.ADMIN);
      ensureRole(com.sa.event_mng.model.enums.Role.ORGANIZER);
      ensureRole(com.sa.event_mng.model.enums.Role.CUSTOMER);
      ensureRole(com.sa.event_mng.model.enums.Role.STAFF);

      if (userRepo.findByUsername("admin").isEmpty()) {
        User user =
            User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .fullName("ADMIN-MANAGERMENT")
                .email("admin@gmail.com")
                .phone("0123456789")
                .roles(new ArrayList<>(List.of(adminRole)))
                .enabled(true)
                .build();
        userRepo.save(user);
        log.info("admin account has been created with default: (username,password) - (admin,admin) , please change it !");
      } else {
        userRepo.findByUsername("admin").ifPresent(user -> {
          if (user.getRoles().stream().noneMatch(role -> role.getName() == com.sa.event_mng.model.enums.Role.ADMIN)) {
            user.getRoles().add(adminRole);
            userRepo.save(user);
          }
        });
        log.info("Admin account already exists");
      }
    };
  }

  private Role ensureRole(com.sa.event_mng.model.enums.Role role) {
    return roleRepository.findByName(role)
        .orElseGet(() -> roleRepository.save(Role.builder().name(role).build()));
  }
}
