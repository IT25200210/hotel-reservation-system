package com.hotel.reservation.config;

import com.hotel.reservation.entity.Role;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.repository.RoleRepository;
import com.hotel.reservation.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(RoleRepository roleRepository, UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create roles if they do not exist
        createRoleIfNotFound("ROLE_ADMIN", "System Administrator");
        createRoleIfNotFound("ROLE_GM", "General Manager");
        createRoleIfNotFound("ROLE_RESERVATIONS", "Reservations Executive");
        createRoleIfNotFound("ROLE_FRONT_OFFICE", "Front Office Staff");
        createRoleIfNotFound("ROLE_FINANCE", "Finance Manager");
        createRoleIfNotFound("ROLE_HOUSEKEEPING", "Housekeeping Staff");

        // Create default admin if not exists
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Admin");
            admin.setEmail("admin@hotel.com");
            admin.setStatus("ACTIVE");
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println(">>> Default admin created: username=admin / password=admin123");
        }
    }

    private void createRoleIfNotFound(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }
}