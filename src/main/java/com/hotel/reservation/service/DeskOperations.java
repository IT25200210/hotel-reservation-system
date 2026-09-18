package com.hotel.reservation.service;

import com.hotel.reservation.entity.*;
import com.hotel.reservation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class DeskOperations {
    private final DeskRoomRepository rooms;
    private final DeskStayRepository stays;
    private final DeskReservationRepository reservations;
    private static final List<String> DEFAULT_ROOM_NUMBERS = List.of(
            "101", "102", "103", "104",
            "201", "202", "203", "204"
    );

    public DeskOperations(DeskRoomRepository rooms, DeskStayRepository stays,
                          DeskReservationRepository reservations) {
        this.rooms = rooms; this.stays = stays; this.reservations = reservations;
    }

    private String actor() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new ResponseStatusException(CONFLICT, message);
    }

    private void money(BigDecimal value) {
        require(value != null && value.signum() > 0 && value.scale() <= 2
                        && value.compareTo(new BigDecimal("9999999999.99")) <= 0,
                "Amount must be positive, at most 2 decimals and within limit");
    }

    @PreAuthorize("hasAnyRole('RESERVATIONS','FRONT_OFFICE')")
    public List<DeskRoom> rooms() {
        seedDefaultRoomsIfEmpty();
        return rooms.findAllByOrderByNumberAsc();
    }

    private void seedDefaultRoomsIfEmpty() {
        if (rooms.count() > 0) return;

        for (String number : DEFAULT_ROOM_NUMBERS) {
            if (!rooms.existsByNumber(number)) {
                rooms.save(new DeskRoom(number));
            }
        }
    }

    private boolean reservationConflict(Long roomId, LocalDate start, LocalDate end) {
        return reservations.existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThan(
                roomId, DeskReservation.Status.CONFIRMED, end, start);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('RESERVATIONS','FRONT_OFFICE')")
    public List<DeskReservation> reservations() {
        return reservations.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('RESERVATIONS','FRONT_OFFICE')")
    public boolean isRoomAvailable(Long roomId, LocalDate arrival, LocalDate departure) {
        require(arrival != null && departure != null && departure.isAfter(arrival),
                "Arrival and departure must be valid");
        return !reservationConflict(roomId, arrival, departure)
                && !stays.existsByRoomIdAndCheckedOutFalseAndArrivalLessThanAndDepartureGreaterThan(
                roomId, departure, arrival);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESERVATIONS')")
    public List<DeskReservation> getHistoryForCustomer(String guestName) {
        require(guestName != null && !guestName.isBlank(), "Guest name is required");
        return reservations.findByGuestNameIgnoreCaseOrderByArrivalDesc(guestName.trim());
    }

    @PreAuthorize("hasRole('RESERVATIONS')")
    public DeskReservation createReservation(Long roomId, String guest, LocalDate arrival,
                                             LocalDate departure, BigDecimal rate) {
        require(guest != null && !guest.isBlank() && guest.trim().length() <= 100,
                "Guest name is required (maximum 100 characters)");
        require(arrival != null && !arrival.isBefore(LocalDate.now())
                        && departure != null && departure.isAfter(arrival),
                "Arrival must be today or later; departure must be after arrival");
        money(rate);
        money(rate.multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(arrival, departure))));

        DeskRoom room = rooms.lockById(roomId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        require(!reservationConflict(roomId, arrival, departure),
                "Room has an overlapping confirmed reservation");
        require(!stays.existsByRoomIdAndCheckedOutFalseAndArrivalLessThanAndDepartureGreaterThan(
                roomId, departure, arrival), "Room has an overlapping active stay");
        require(!arrival.equals(LocalDate.now()) || !room.isOccupied(),
                "Room is currently occupied");

        return reservations.save(new DeskReservation(room, guest.trim(), arrival, departure, rate));
    }

    @PreAuthorize("hasRole('RESERVATIONS')")
    public DeskReservation modifyReservation(Long id, Long roomId, String guest,
                                             LocalDate arrival, LocalDate departure, BigDecimal rate) {
        require(guest != null && !guest.isBlank() && guest.trim().length() <= 100,
                "Guest name is required (maximum 100 characters)");
        require(arrival != null && !arrival.isBefore(LocalDate.now())
                        && departure != null && departure.isAfter(arrival),
                "Arrival must be today or later; departure must be after arrival");
        money(rate);

        DeskReservation reservation = reservations.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        require(reservation.getStatus() == DeskReservation.Status.CONFIRMED,
                "Only confirmed reservations can be modified");

        DeskRoom room = rooms.lockById(roomId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        boolean conflict = reservations
                .existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThanAndIdNot(
                        roomId, DeskReservation.Status.CONFIRMED, departure, arrival, id);
        require(!conflict, "Room has an overlapping confirmed reservation");

        reservation.update(room, guest.trim(), arrival, departure, rate);
        return reservation;
    }

    @PreAuthorize("hasRole('RESERVATIONS')")
    public void cancelReservation(Long id) {
        DeskReservation reservation = reservations.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        require(reservation.getStatus() == DeskReservation.Status.CONFIRMED,
                "Only confirmed reservations can be cancelled");
        reservation.cancel();
    }
}
