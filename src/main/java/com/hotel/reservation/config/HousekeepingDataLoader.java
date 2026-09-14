package com.hotel.reservation.config;

import com.hotel.reservation.entity.HousekeepingTask;
import com.hotel.reservation.entity.MaintenanceRequest;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.repository.HousekeepingTaskRepository;
import com.hotel.reservation.repository.MaintenanceRequestRepository;
import com.hotel.reservation.repository.RoleRepository;
import com.hotel.reservation.repository.UserRepository;
import com.hotel.reservation.service.RoomCleaningStatusService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds demo rooms/tasks and ensures housekeeping users are available
@Component
@Order(2)
public class HousekeepingDataLoader implements CommandLineRunner {

    private final RoomCleaningStatusService roomStatusService;
    private final HousekeepingTaskRepository taskRepository;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public HousekeepingDataLoader(RoomCleaningStatusService roomStatusService,
                                  HousekeepingTaskRepository taskRepository,
                                  MaintenanceRequestRepository maintenanceRepository,
                                  UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder) {
        this.roomStatusService = roomStatusService;
        this.taskRepository = taskRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        roomStatusService.ensureRoom("101");
        roomStatusService.ensureRoom("102");
        roomStatusService.ensureRoom("103");
        roomStatusService.ensureRoom("104");

        // Ensure official housekeeping user exists
        if (!userRepository.existsByUsername("housekeeping")) {
            roleRepository.findByName("ROLE_HOUSEKEEPING").ifPresent(role -> {
                User hk = new User();
                hk.setUsername("housekeeping");
                hk.setPassword(passwordEncoder.encode("hk123"));
                hk.setFullName("Housekeeping Staff");
                hk.setEmail("housekeeping@hotel.com");
                hk.setStatus("ACTIVE");
                hk.setRole(role);
                userRepository.save(hk);
            });
        }

        // Ensure hkstaff exists as a secondary user in addition to dev's "housekeeping"
        if (!userRepository.existsByUsername("hkstaff")) {
            roleRepository.findByName("ROLE_HOUSEKEEPING").ifPresent(role -> {
                User hk = new User();
                hk.setUsername("hkstaff");
                hk.setPassword(passwordEncoder.encode("hk123"));
                hk.setFullName("Housekeeping Staff");
                hk.setEmail("hkstaff@hotel.com");
                hk.setStatus("ACTIVE");
                hk.setRole(role);
                userRepository.save(hk);
            });
        }

        if (taskRepository.count() == 0) {
            HousekeepingTask task = new HousekeepingTask();
            task.setRoomNumber("101");
            task.setAssignedTo("housekeeping");
            task.setNotes("Checkout cleaning - change linens, restock minibar");
            taskRepository.save(task);
        }

        if (maintenanceRepository.count() == 0) {
            MaintenanceRequest request = new MaintenanceRequest();
            request.setRoomNumber("103");
            request.setIssue("AC not cooling, makes rattling noise");
            request.setReportedBy("housekeeping");
            maintenanceRepository.save(request);
        }
    }
}
