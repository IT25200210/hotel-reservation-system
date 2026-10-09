package com.hotel.reservation.config;

import com.hotel.reservation.entity.DeskRoom;
import com.hotel.reservation.entity.HousekeepingTask;
import com.hotel.reservation.entity.MaintenanceRequest;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.repository.DeskRoomRepository;
import com.hotel.reservation.repository.HousekeepingTaskRepository;
import com.hotel.reservation.repository.MaintenanceRequestRepository;
import com.hotel.reservation.repository.RoleRepository;
import com.hotel.reservation.repository.UserRepository;
import com.hotel.reservation.service.RoomCleaningStatusService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
    private final DeskRoomRepository deskRoomRepository;

    public HousekeepingDataLoader(RoomCleaningStatusService roomStatusService,
                                  HousekeepingTaskRepository taskRepository,
                                  MaintenanceRequestRepository maintenanceRepository,
                                  UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  DeskRoomRepository deskRoomRepository) {
        this.roomStatusService = roomStatusService;
        this.taskRepository = taskRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.deskRoomRepository = deskRoomRepository;
    }

    @Override
    public void run(String... args) {
        // Ensure hotel rooms exist in Bevon's DeskRoom entity
        ensureDeskRoom("101", DeskRoom.RoomType.SINGLE, new BigDecimal("120.00"), 1);
        ensureDeskRoom("102", DeskRoom.RoomType.DOUBLE, new BigDecimal("180.00"), 2);
        ensureDeskRoom("103", DeskRoom.RoomType.DELUXE, new BigDecimal("250.00"), 3);
        ensureDeskRoom("104", DeskRoom.RoomType.SUITE, new BigDecimal("400.00"), 4);
        ensureDeskRoom("201", DeskRoom.RoomType.SINGLE, new BigDecimal("130.00"), 1);
        ensureDeskRoom("202", DeskRoom.RoomType.DOUBLE, new BigDecimal("190.00"), 2);
        ensureDeskRoom("203", DeskRoom.RoomType.DELUXE, new BigDecimal("270.00"), 3);

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

    private void ensureDeskRoom(String number, DeskRoom.RoomType type, BigDecimal rate, int capacity) {
        if (!deskRoomRepository.existsByNumber(number)) {
            DeskRoom room = new DeskRoom();
            room.setNumber(number);
            room.setType(type);
            room.setNightlyRate(rate);
            room.setCapacity(capacity);
            room.setActive(true);
            deskRoomRepository.save(room);
        }
    }
}
