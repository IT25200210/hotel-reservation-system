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

    public DeskOperations(DeskRoomRepository rooms,
                          DeskStayRepository stays,
                          DeskReservationRepository reservations) {
        this.rooms = rooms;
        this.stays = stays;
        this.reservations = reservations;
    }

    private String actor() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(CONFLICT, message);
        }
    }

    private void money(BigDecimal value) {
        require(
                value != null
                        && value.signum() > 0
                        && value.scale() <= 2
                        && value.compareTo(new BigDecimal("9999999999.99")) <= 0,
                "Amount must be positive, at most 2 decimals and within limit"
        );
    }

    // READ - get rooms from database
    @PreAuthorize("hasAnyRole('RESERVATIONS','FRONT_OFFICE')")
    public List<DeskRoom> rooms() {
        return rooms.findAllByOrderByNumberAsc();
    }

    private boolean reservationConflict(Long roomId,
                                        LocalDate start,
                                        LocalDate end) {

        return reservations
                .existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThan(
                        roomId,
                        DeskReservation.Status.CONFIRMED,
                        end,
                        start
                );
    }

    // READ - reservations
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('RESERVATIONS','FRONT_OFFICE')")
    public List<DeskReservation> reservations() {
        return reservations.findAllByOrderByIdDesc();
    }

    // Check room availability
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('RESERVATIONS','FRONT_OFFICE')")
    public boolean isRoomAvailable(Long roomId,
                                   LocalDate arrival,
                                   LocalDate departure) {

        require(
                arrival != null
                        && departure != null
                        && departure.isAfter(arrival),
                "Arrival and departure must be valid"
        );

        return !reservationConflict(roomId, arrival, departure)
                && !stays.existsByRoomIdAndCheckedOutFalseAndArrivalLessThanAndDepartureGreaterThan(
                roomId,
                departure,
                arrival
        );
    }

    // Customer reservation history
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESERVATIONS')")
    public List<DeskReservation> getHistoryForCustomer(String guestName) {

        require(
                guestName != null && !guestName.isBlank(),
                "Guest name is required"
        );

        return reservations
                .findByGuestNameIgnoreCaseOrderByArrivalDesc(
                        guestName.trim()
                );
    }

    // CREATE reservation
    @PreAuthorize("hasRole('RESERVATIONS')")
    public DeskReservation createReservation(Long roomId,
                                             String guest,
                                             LocalDate arrival,
                                             LocalDate departure,
                                             BigDecimal ignoredRate) {

        require(
                guest != null
                        && !guest.isBlank()
                        && guest.trim().length() <= 100,
                "Guest name is required (maximum 100 characters)"
        );

        require(
                arrival != null
                        && !arrival.isBefore(LocalDate.now())
                        && departure != null
                        && departure.isAfter(arrival),
                "Arrival must be today or later; departure must be after arrival"
        );

        // Get the selected room from the database
        DeskRoom room = rooms.lockById(roomId)
                .orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND)
                );

        // Get the fixed nightly rate of that room
        BigDecimal rate = room.getNightlyRate();

        // Validate room rate
        money(rate);

        // Calculate total rate
        long numberOfNights =
                ChronoUnit.DAYS.between(arrival, departure);

        BigDecimal totalRate =
                rate.multiply(BigDecimal.valueOf(numberOfNights));

        money(totalRate);

        require(
                !reservationConflict(
                        roomId,
                        arrival,
                        departure
                ),
                "Room has an overlapping confirmed reservation"
        );

        require(
                !stays.existsByRoomIdAndCheckedOutFalseAndArrivalLessThanAndDepartureGreaterThan(
                        roomId,
                        departure,
                        arrival
                ),
                "Room has an overlapping active stay"
        );

        require(
                !arrival.equals(LocalDate.now())
                        || !room.isOccupied(),
                "Room is currently occupied"
        );

        return reservations.save(
                new DeskReservation(
                        room,
                        guest.trim(),
                        arrival,
                        departure,
                        rate
                )
        );
    }

    // UPDATE reservation
    @PreAuthorize("hasRole('RESERVATIONS')")
    public DeskReservation modifyReservation(Long id,
                                             Long roomId,
                                             String guest,
                                             LocalDate arrival,
                                             LocalDate departure,
                                             BigDecimal ignoredRate) {

        require(
                guest != null
                        && !guest.isBlank()
                        && guest.trim().length() <= 100,
                "Guest name is required (maximum 100 characters)"
        );

        require(
                arrival != null
                        && !arrival.isBefore(LocalDate.now())
                        && departure != null
                        && departure.isAfter(arrival),
                "Arrival must be today or later; departure must be after arrival"
        );

        DeskReservation reservation = reservations.lockById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND)
                );

        require(
                reservation.getStatus()
                        == DeskReservation.Status.CONFIRMED,
                "Only confirmed reservations can be modified"
        );

        DeskRoom room = rooms.lockById(roomId)
                .orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND)
                );

        // Get the selected room's fixed nightly rate
        BigDecimal rate = room.getNightlyRate();

        money(rate);

        boolean conflict = reservations
                .existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThanAndIdNot(
                        roomId,
                        DeskReservation.Status.CONFIRMED,
                        departure,
                        arrival,
                        id
                );

        require(
                !conflict,
                "Room has an overlapping confirmed reservation"
        );

        reservation.update(
                room,
                guest.trim(),
                arrival,
                departure,
                rate
        );

        return reservation;
    }

    // CANCEL reservation
    @PreAuthorize("hasRole('RESERVATIONS')")
    public void cancelReservation(Long id) {

        DeskReservation reservation = reservations.lockById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND)
                );

        require(
                reservation.getStatus()
                        == DeskReservation.Status.CONFIRMED,
                "Only confirmed reservations can be cancelled"
        );

        reservation.cancel();
    }
}