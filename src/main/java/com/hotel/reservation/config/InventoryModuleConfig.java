package com.hotel.reservation.config;
import com.hotel.reservation.entity.Role;
import com.hotel.reservation.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class InventoryModuleConfig {
    @Bean
    CommandLineRunner inventoryRoleInitializer(RoleRepository roles) {
        return args -> {
            if (roles.findByName("ROLE_INVENTORY").isEmpty()) {
                Role role = new Role();
                role.setName("ROLE_INVENTORY");
                role.setDescription("Inventory Manager");
                roles.save(role);
            }
        };
    }
}
