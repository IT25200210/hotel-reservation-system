package com.hotel.reservation.config;

import com.hotel.reservation.entity.HousekeepingTask;
import com.hotel.reservation.entity.MaintenanceRequest;
import com.hotel.reservation.repository.HousekeepingTaskRepository;
import com.hotel.reservation.repository.MaintenanceRequestRepository;
import com.hotel.reservation.service.RoomCleaningStatusService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Seeds a few demo rooms/tasks so the housekeeping module can be tested
// before the GM module (which will own room creation) is built.
@Component
@Order(2)
public class HousekeepingDataLoader implements CommandLineRunner {

    private final RoomCleaningStatusService roomStatusService;
    private final HousekeepingTaskRepository taskRepository;
    private final MaintenanceRequestRepository maintenanceRepository;

    public HousekeepingDataLoader(RoomCleaningStatusService roomStatusService,
                                  HousekeepingTaskRepository taskRepository,
                                  MaintenanceRequestRepository maintenanceRepository) {
        this.roomStatusService = roomStatusService;
        this.taskRepository = taskRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    public void run(String... args) {
        roomStatusService.ensureRoom("101");
        roomStatusService.ensureRoom("102");
        roomStatusService.ensureRoom("103");
        roomStatusService.ensureRoom("104");

        if (taskRepository.count() == 0) {
            HousekeepingTask task = new HousekeepingTask();
            task.setRoomNumber("101");
            task.setAssignedTo("hkstaff");
            task.setNotes("Checkout cleaning - change linens, restock minibar");
            taskRepository.save(task);
        }

        if (maintenanceRepository.count() == 0) {
            MaintenanceRequest request = new MaintenanceRequest();
            request.setRoomNumber("103");
            request.setIssue("AC not cooling, makes rattling noise");
            request.setReportedBy("hkstaff");
            maintenanceRepository.save(request);
        }
    }
}
