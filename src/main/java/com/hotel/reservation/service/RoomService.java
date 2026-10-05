package com.hotel.reservation.service;

import com.hotel.reservation.entity.DeskRoom;
import com.hotel.reservation.repository.DeskRoomRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final DeskRoomRepository roomRepository;
    private final RoomCleaningStatusService roomCleaningStatusService;
    private final AuditLogService auditLogService;

    public RoomService(DeskRoomRepository roomRepository,
                       RoomCleaningStatusService roomCleaningStatusService,
                       AuditLogService auditLogService) {
        this.roomRepository = roomRepository;
        this.roomCleaningStatusService = roomCleaningStatusService;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasRole('GM')")
    public List<DeskRoom> getAllRooms() {
        return roomRepository.findAllByOrderByNumberAsc();
    }

    @PreAuthorize("hasRole('GM')")
    public DeskRoom getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    @PreAuthorize("hasRole('GM')")
    public DeskRoom saveRoom(DeskRoom room) {
        validateRoomNumber(room);

        DeskRoom saved = roomRepository.save(room);

        if (saved.isActive()) {
            roomCleaningStatusService.ensureRoom(saved.getNumber());
        }

        auditLogService.log(actor(), "SAVE", "DeskRoom", saved.getId(),
                "Saved room " + saved.getNumber());

        return saved;
    }

    @PreAuthorize("hasRole('GM')")
    public void deactivateRoom(Long id) {
        DeskRoom room = getRoomById(id);
        room.setActive(false);
        roomRepository.save(room);

        auditLogService.log(actor(), "DEACTIVATE", "DeskRoom", room.getId(),
                "Deactivated room " + room.getNumber());
    }

    private String actor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "system";
    }

    private void validateRoomNumber(DeskRoom room) {
        String number = room.getNumber();

        if (number == null || number.isBlank()) {
            throw new RuntimeException("Room number is required");
        }

        room.setNumber(number.trim());

        if (room.getId() == null && roomRepository.existsByNumber(room.getNumber())) {
            throw new RuntimeException("Room number already exists");
        }

        if (room.getId() != null && roomRepository.existsByNumberAndIdNot(room.getNumber(), room.getId())) {
            throw new RuntimeException("Room number already exists");
        }
    }
}