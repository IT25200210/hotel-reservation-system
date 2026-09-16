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
        // Create all roles
        createRoleIfNotFound("ROLE_ADMIN", "System Administrator");
        createRoleIfNotFound("ROLE_GM", "General Manager");
        createRoleIfNotFound("ROLE_RESERVATIONS", "Reservations Executive");
        createRoleIfNotFound("ROLE_FRONT_OFFICE", "Front Office Staff");
        createRoleIfNotFound("ROLE_FINANCE", "Finance Manager");
        createRoleIfNotFound("ROLE_HOUSEKEEPING", "Housekeeping Staff");

        // Create default users for every role
        createUserIfNotFound("admin", "admin123", "System Admin", "admin@hotel.com", "ROLE_ADMIN");
        createUserIfNotFound("gm", "gm123", "General Manager", "gm@hotel.com", "ROLE_GM");
        createUserIfNotFound("reservations", "res123", "Reservations Executive", "reservations@hotel.com", "ROLE_RESERVATIONS");
        createUserIfNotFound("frontoffice", "fo123", "Front Office Staff", "frontoffice@hotel.com", "ROLE_FRONT_OFFICE");
        createUserIfNotFound("finance", "fin123", "Finance Manager", "finance@hotel.com", "ROLE_FINANCE");
        createUserIfNotFound("housekeeping", "hk123", "Housekeeping Staff", "housekeeping@hotel.com", "ROLE_HOUSEKEEPING");
    }

    private void createRoleIfNotFound(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }

    private void createUserIfNotFound(String username, String password, String fullName,
                                      String email, String roleName) {
        if (!userRepository.existsByUsername(username)) {
            Role role = roleRepository.findByName(roleName).orElseThrow();
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setStatus("ACTIVE");
            user.setRole(role);
            userRepository.save(user);
            System.out.println(">>> Created user: " + username + " / " + password);
        }
    }
}