package com.hotel.reservation.service;

import com.hotel.reservation.entity.HousekeepingTask;
import com.hotel.reservation.entity.RoomCleaningStatus;
import com.hotel.reservation.repository.HousekeepingTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HousekeepingTaskService {

    private final HousekeepingTaskRepository taskRepository;
    private final RoomCleaningStatusService roomStatusService;
    private final AuditLogService auditLogService;

    public HousekeepingTaskService(HousekeepingTaskRepository taskRepository,
                                   RoomCleaningStatusService roomStatusService,
                                   AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.roomStatusService = roomStatusService;
        this.auditLogService = auditLogService;
    }

    public List<HousekeepingTask> getTasks(String status, String roomNumber) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasRoom = roomNumber != null && !roomNumber.isBlank();
        if (hasStatus && hasRoom) {
            return taskRepository.findByStatusAndRoomNumberContainingIgnoreCase(status, roomNumber);
        } else if (hasStatus) {
            return taskRepository.findByStatus(status);
        } else if (hasRoom) {
            return taskRepository.findByRoomNumberContainingIgnoreCase(roomNumber);
        }
        return taskRepository.findAll();
    }

    public HousekeepingTask getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Housekeeping task not found"));
    }

    public HousekeepingTask createTask(HousekeepingTask task) {
        task.setId(null);
        task.setStatus(HousekeepingTask.STATUS_PENDING);
        // make sure the room exists on the status board as DIRTY
        roomStatusService.ensureRoom(task.getRoomNumber());
        HousekeepingTask saved = taskRepository.save(task);
        auditLogService.log("system", "CREATE", "HousekeepingTask", saved.getId(),
                "Created cleaning task for room " + saved.getRoomNumber());
        return saved;
    }

    public HousekeepingTask updateTask(Long id, HousekeepingTask updated) {
        HousekeepingTask existing = getTaskById(id);
        existing.setRoomNumber(updated.getRoomNumber());
        existing.setAssignedTo(updated.getAssignedTo());
        existing.setNotes(updated.getNotes());
        if (updated.getStatus() != null && !updated.getStatus().isBlank()) {
            existing.setStatus(updated.getStatus());
        }
        syncRoomStatus(existing);
        return taskRepository.save(existing);
    }

    public HousekeepingTask updateTaskStatus(Long id, String status) {
        HousekeepingTask task = getTaskById(id);
        task.setStatus(status);
        syncRoomStatus(task);
        HousekeepingTask saved = taskRepository.save(task);
        auditLogService.log("system", "UPDATE", "HousekeepingTask", saved.getId(),
                "Task status changed to " + status + " for room " + saved.getRoomNumber());
        return saved;
    }

    public void deleteTask(Long id) {
        HousekeepingTask task = getTaskById(id);
        taskRepository.delete(task);
        auditLogService.log("system", "DELETE", "HousekeepingTask", id,
                "Deleted cleaning task for room " + task.getRoomNumber());
    }

    // Keep the room cleaning status in sync with the task lifecycle:
    // PENDING -> DIRTY, IN_PROGRESS -> IN_PROGRESS, COMPLETED -> CLEAN
    private void syncRoomStatus(HousekeepingTask task) {
        RoomCleaningStatus room = roomStatusService.ensureRoom(task.getRoomNumber());
        switch (task.getStatus()) {
            case HousekeepingTask.STATUS_PENDING -> roomStatusService.updateStatus(room.getId(), RoomCleaningStatus.STATUS_DIRTY);
            case HousekeepingTask.STATUS_IN_PROGRESS -> roomStatusService.updateStatus(room.getId(), RoomCleaningStatus.STATUS_IN_PROGRESS);
            case HousekeepingTask.STATUS_COMPLETED -> roomStatusService.updateStatus(room.getId(), RoomCleaningStatus.STATUS_CLEAN);
            default -> { }
        }
    }
}
