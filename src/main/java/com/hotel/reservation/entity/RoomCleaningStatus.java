package com.hotel.reservation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "room_cleaning_status")
public class RoomCleaningStatus {

    public static final String STATUS_DIRTY = "DIRTY";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_CLEAN = "CLEAN";
    public static final String STATUS_INSPECTED = "INSPECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Column(nullable = false)
    private String status = STATUS_DIRTY; // DIRTY, IN_PROGRESS, CLEAN, INSPECTED

    private LocalDateTime updatedAt = LocalDateTime.now();

    // --- getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedAtFormatted() {
        return updatedAt == null ? "" : updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
