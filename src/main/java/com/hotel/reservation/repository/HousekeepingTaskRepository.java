package com.hotel.reservation.repository;

import com.hotel.reservation.entity.HousekeepingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, Long> {
    List<HousekeepingTask> findByStatus(String status);
    List<HousekeepingTask> findByRoomNumberContainingIgnoreCase(String roomNumber);
    List<HousekeepingTask> findByStatusAndRoomNumberContainingIgnoreCase(String status, String roomNumber);
}
