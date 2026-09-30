package com.hotel.reservation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "desk_rooms")
public class DeskRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String number;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal nightlyRate;

    @Column(nullable = false)
    private boolean occupied;

    protected DeskRoom() {}

    public DeskRoom(String number, BigDecimal nightlyRate) {
        this.number = number;
        this.nightlyRate = nightlyRate;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getNightlyRate() {
        return nightlyRate;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}