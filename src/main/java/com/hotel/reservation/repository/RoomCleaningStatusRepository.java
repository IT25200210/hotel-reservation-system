package com.hotel.reservation.repository;

import com.hotel.reservation.entity.RoomCleaningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoomCleaningStatusRepository extends JpaRepository<RoomCleaningStatus, Long> {
    Optional<RoomCleaningStatus> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);
}
