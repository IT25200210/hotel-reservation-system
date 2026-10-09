package com.hotel.reservation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "desk_rooms")
public class DeskRoom {

    public enum RoomType {
        SINGLE, DOUBLE, DELUXE, SUITE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room number is required")
    @Column(nullable = false, unique = true, length = 20)
    private String number;

    @NotNull(message = "Room type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type = RoomType.SINGLE;

    @NotNull(message = "Nightly rate is required")
    @DecimalMin(value = "0.01", message = "Nightly rate must be greater than 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal nightlyRate = BigDecimal.ZERO;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(nullable = false)
    private int capacity = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean occupied = false;

    public DeskRoom() {
    }

    public DeskRoom(String number) {
        this.number = number == null ? null : number.trim();
        this.type = RoomType.SINGLE;
        this.nightlyRate = new BigDecimal("1.00");
        this.capacity = 1;
        this.active = true;
        this.occupied = false;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public RoomType getType() {
        return type;
    }

    public BigDecimal getNightlyRate() {
        return nightlyRate;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public void setNightlyRate(BigDecimal nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}