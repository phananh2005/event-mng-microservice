package com.sa.authservice.faker;

import com.sa.authservice.model.entity.Role;
import com.sa.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository roleRepository;

    public void seed() {
        for (com.sa.authservice.model.enums.Role r : com.sa.authservice.model.enums.Role.values()) {
            if (roleRepository.findByName(r).isEmpty()) {
                roleRepository.save(Role.builder().name(r).build());
            }
        }
    }
}
