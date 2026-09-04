package com.hotel.reservation.service;
import com.hotel.reservation.entity.Role;
import com.hotel.reservation.repository.RoleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role updated) {
        Role existing = getRoleById(id);
        existing.setDescription(updated.getDescription());
        // name of system roles should not be changed lightly
        return roleRepository.save(existing);
    }
}