package com.hotel.reservation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "desk_payments")
public class DeskPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stay_id", nullable = false)
    private DeskStay stay;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, unique = true, length = 36)
    private String requestKey;

    @Column(nullable = false, length = 100)
    private String recordedBy;

    @Column(nullable = false)
    private Instant recordedAt = Instant.now();

    protected DeskPayment() {}

    public DeskPayment(DeskStay stay, BigDecimal amount,
                       String requestKey, String recordedBy) {
        this.stay = stay;
        this.amount = amount;
        this.requestKey = requestKey;
        this.recordedBy = recordedBy;
    }

    public Long getId() {
        return id;
    }

    public DeskStay getStay() {
        return stay;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
