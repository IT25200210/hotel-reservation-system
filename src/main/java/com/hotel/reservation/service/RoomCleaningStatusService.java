package com.hotel.reservation.service;

import com.hotel.reservation.entity.RoomCleaningStatus;
import com.hotel.reservation.repository.RoomCleaningStatusRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomCleaningStatusService {

    private final RoomCleaningStatusRepository roomStatusRepository;

    public RoomCleaningStatusService(RoomCleaningStatusRepository roomStatusRepository) {
        this.roomStatusRepository = roomStatusRepository;
    }

    public List<RoomCleaningStatus> getAllRooms() {
        return roomStatusRepository.findAll();
    }

    public RoomCleaningStatus getRoomById(Long id) {
        return roomStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room cleaning status not found"));
    }

    // Rooms are created implicitly (in future by the GM module when a room is added).
    // Until then this guarantees the room exists on the status board.
    public RoomCleaningStatus ensureRoom(String roomNumber) {
        return roomStatusRepository.findByRoomNumber(roomNumber)
                .orElseGet(() -> {
                    RoomCleaningStatus room = new RoomCleaningStatus();
                    room.setRoomNumber(roomNumber);
                    room.setStatus(RoomCleaningStatus.STATUS_DIRTY);
                    return roomStatusRepository.save(room);
                });
    }

    public RoomCleaningStatus createRoom(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new RuntimeException("Room number is required");
        }
        if (roomStatusRepository.existsByRoomNumber(roomNumber.trim())) {
            throw new RuntimeException("Room " + roomNumber + " already exists");
        }
        return ensureRoom(roomNumber.trim());
    }

    public RoomCleaningStatus updateStatus(Long id, String status) {
        RoomCleaningStatus room = getRoomById(id);
        room.setStatus(status);
        room.setUpdatedAt(LocalDateTime.now());
        return roomStatusRepository.save(room);
    }
}
