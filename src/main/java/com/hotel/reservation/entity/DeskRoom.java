package com.hotel.reservation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "desk_rooms")
public class DeskRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String number;

    @Column(nullable = false)
    private boolean occupied;

    protected DeskRoom() {}

    public DeskRoom(String number) { this.number = number.trim(); }

    public Long getId() { return id; }
    public String getNumber() { return number; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
}
