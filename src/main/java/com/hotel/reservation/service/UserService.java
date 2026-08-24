package com.hotel.reservation.service;
import com.hotel.reservation.entity.Role;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.repository.RoleRepository;
import com.hotel.reservation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User createUser(User user, Long roleId) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("ACTIVE");
        return userRepository.save(user);
        auditLogService.log("system", "CREATE", "User", saved.getId(),
                "Created user: " + saved.getUsername());
    }

    public User updateUser(Long id, User updated, Long roleId) {
        User existing = getUserById(id);
        existing.setFullName(updated.getFullName());
        existing.setEmail(updated.getEmail());
        if (roleId != null) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            existing.setRole(role);
        }
        // only change password if a new one was provided
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        return userRepository.save(existing);
    }

    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setStatus("DEACTIVATED");
        userRepository.save(user);
        auditLogService.log("system", "DEACTIVATE", "User", id,
                "Deactivated user id: " + id);
    }

    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }
}