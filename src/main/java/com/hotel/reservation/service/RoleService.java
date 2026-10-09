package com.hotel.reservation.service;
import com.hotel.reservation.entity.Role;
import com.hotel.reservation.repository.RoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;

    public RoleService(RoleRepository roleRepository, AuditLogService auditLogService) {
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role createRole(Role role) {
        Role saved = roleRepository.save(role);
        auditLogService.log(actor(), "CREATE", "Role", saved.getId(),
                "Created role: " + saved.getName());
        return saved;
    }

    public Role updateRole(Long id, Role updated) {
        Role existing = getRoleById(id);
        existing.setDescription(updated.getDescription());
        // name of system roles should not be changed lightly
        Role saved = roleRepository.save(existing);
        auditLogService.log(actor(), "UPDATE", "Role", saved.getId(),
                "Updated role: " + saved.getName());
        return saved;
    }

    private String actor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "system";
    }
}