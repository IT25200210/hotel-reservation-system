package com.hotel.reservation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "desk_stays")
public class DeskStay {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private DeskRoom room;

    @Column(nullable = false, length = 100)
    private String guestName;

    @Column(nullable = false)
    private LocalDate arrival;

    @Column(nullable = false)
    private LocalDate departure;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paid = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean checkedOut;

    protected DeskStay() {}

    public DeskStay(DeskRoom room, String guestName, LocalDate arrival, LocalDate departure, BigDecimal total) {
        this.room = room; this.guestName = guestName;
        this.arrival = arrival; this.departure = departure;
        this.total = total;
    }

    public Long getId() { return id; }
    public DeskRoom getRoom() { return room; }
    public String getGuestName() { return guestName; }
    public LocalDate getArrival() { return arrival; }
    public LocalDate getDeparture() { return departure; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getPaid() { return paid; }
    public BigDecimal getBalance() { return total.subtract(paid); }
    public boolean isCheckedOut() { return checkedOut; }
    public void addPayment(BigDecimal amount) { paid = paid.add(amount); }
    public void checkOut() { checkedOut = true; }
}