package com.hotel.reservation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "desk_reservations")
public class DeskReservation {
    public enum Status { CONFIRMED, CANCELLED, CHECKED_IN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private DeskRoom room;

    @Column(nullable = false, length = 100)
    private String guestName;

    @Column(nullable = false)
    private LocalDate arrival;

    @Column(nullable = false)
    private LocalDate departure;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal nightlyRate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.CONFIRMED;

    @OneToOne
    private DeskStay stay;

    protected DeskReservation() {}

    public DeskReservation(DeskRoom room, String guestName, LocalDate arrival, LocalDate departure, BigDecimal nightlyRate) {
        this.room = room; this.guestName = guestName;
        this.arrival = arrival; this.departure = departure;
        this.nightlyRate = nightlyRate;
    }

    public Long getId() { return id; }
    public DeskRoom getRoom() { return room; }
    public String getGuestName() { return guestName; }
    public LocalDate getArrival() { return arrival; }
    public LocalDate getDeparture() { return departure; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public Status getStatus() { return status; }
    public DeskStay getStay() { return stay; }
    public void cancel() { status = Status.CANCELLED; }
    public void checkIn(DeskStay stay) { this.stay = stay; status = Status.CHECKED_IN; }
}
