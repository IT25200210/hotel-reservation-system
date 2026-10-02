package com.hotel.reservation.service;

import com.hotel.reservation.entity.DeskReservation;
import com.hotel.reservation.entity.DeskRoom;
import com.hotel.reservation.repository.DeskReservationRepository;
import com.hotel.reservation.repository.DeskRoomRepository;
import com.hotel.reservation.repository.DeskStayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Public (unauthenticated) booking flow. Mirrors the staff rules in
 * DeskOperations but has no @PreAuthorize so guests can use it via /book.
 */
@Service
@Transactional
public class GuestBookingService {

    private final DeskRoomRepository rooms;
    private final DeskStayRepository stays;
    private final DeskReservationRepository reservations;

    public GuestBookingService(DeskRoomRepository rooms,
                               DeskStayRepository stays,
                               DeskReservationRepository reservations) {
        this.rooms = rooms;
        this.stays = stays;
        this.reservations = reservations;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(CONFLICT, message);
        }
    }

    private void money(BigDecimal value) {
        require(value != null
                        && value.signum() > 0
                        && value.scale() <= 2
                        && value.compareTo(new BigDecimal("9999999999.99")) <= 0,
                "Amount must be positive, at most 2 decimals and within limit");
    }

    @Transactional(readOnly = true)
    public List<DeskRoom> listRooms() {
        return rooms.findAllByOrderByNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<DeskRoom> listRooms(DeskRoom.RoomType type) {
        if (type == null) {
            return listRooms();
        }
        return rooms.findByTypeOrderByNumberAsc(type);
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(Long roomId, LocalDate arrival, LocalDate departure) {
        require(arrival != null && departure != null && departure.isAfter(arrival),
                "Arrival and departure must be valid");
        boolean reservationConflict = reservations
                .existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThan(
                        roomId, DeskReservation.Status.CONFIRMED, departure, arrival);
        boolean stayConflict = stays
                .existsByRoomIdAndCheckedOutFalseAndArrivalLessThanAndDepartureGreaterThan(
                        roomId, departure, arrival);
        return !reservationConflict && !stayConflict;
    }

    public DeskReservation create(Long roomId, String guest,
                                  LocalDate arrival, LocalDate departure) {
        require(guest != null && !guest.isBlank() && guest.trim().length() <= 100,
                "Guest name is required (maximum 100 characters)");
        require(arrival != null && !arrival.isBefore(LocalDate.now())
                        && departure != null && departure.isAfter(arrival),
                "Arrival must be today or later; departure must be after arrival");

        DeskRoom room = rooms.lockById(roomId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Room not found"));
        BigDecimal rate = room.getNightlyRate();
        money(rate);
        long nights = ChronoUnit.DAYS.between(arrival, departure);
        money(rate.multiply(BigDecimal.valueOf(nights)));

        require(!reservations.existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThan(
                        roomId, DeskReservation.Status.CONFIRMED, departure, arrival),
                "Room has an overlapping confirmed reservation");
        require(!stays.existsByRoomIdAndCheckedOutFalseAndArrivalLessThanAndDepartureGreaterThan(
                        roomId, departure, arrival),
                "Room has an overlapping active stay");
        require(!arrival.equals(LocalDate.now()) || !room.isOccupied(),
                "Room is currently occupied");

        DeskReservation created =
                reservations.save(new DeskReservation(room, guest.trim(), arrival, departure, rate));
        // Simple non-production reference: GH-0001, GH-0002, ...
        created.setReference(String.format("GH-%04d", created.getId()));
        return created;
    }

    @Transactional(readOnly = true)
    public DeskReservation lookup(String reference) {
        require(reference != null && !reference.isBlank(), "Reference number is required");
        return reservations.findByReferenceIgnoreCase(reference.trim())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking not found"));
    }

    public DeskReservation modify(String reference, Long roomId, String guest,
                                  LocalDate arrival, LocalDate departure) {
        DeskReservation current = lookup(reference);
        require(current.getStatus() == DeskReservation.Status.CONFIRMED,
                "Only confirmed reservations can be modified");
        require(guest != null && !guest.isBlank() && guest.trim().length() <= 100,
                "Guest name is required (maximum 100 characters)");
        require(arrival != null && !arrival.isBefore(LocalDate.now())
                        && departure != null && departure.isAfter(arrival),
                "Arrival must be today or later; departure must be after arrival");

        DeskReservation reservation = reservations.lockById(current.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking not found"));
        DeskRoom room = rooms.lockById(roomId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Room not found"));
        BigDecimal rate = room.getNightlyRate();
        money(rate);

        boolean conflict = reservations
                .existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThanAndIdNot(
                        roomId, DeskReservation.Status.CONFIRMED, departure, arrival,
                        reservation.getId());
        require(!conflict, "Room has an overlapping confirmed reservation");

        reservation.update(room, guest.trim(), arrival, departure, rate);
        return reservation;
    }

    public void cancel(String reference) {
        DeskReservation current = lookup(reference);
        DeskReservation reservation = reservations.lockById(current.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking not found"));
        require(reservation.getStatus() == DeskReservation.Status.CONFIRMED,
                "Only confirmed reservations can be cancelled");
        reservation.cancel();
    }
}
