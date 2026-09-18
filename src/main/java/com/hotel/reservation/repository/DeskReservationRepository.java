package com.hotel.reservation.repository;

import com.hotel.reservation.entity.DeskReservation;
import com.hotel.reservation.entity.DeskReservation.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.*;

public interface DeskReservationRepository
        extends JpaRepository<DeskReservation, Long> {

    List<DeskReservation> findAllByOrderByIdDesc();

    boolean existsByRoomIdAndStatusAndArrivalLessThanAndDepartureGreaterThan(
            Long roomId,
            Status status,
            LocalDate end,
            LocalDate start);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from DeskReservation r where r.id = :id")
    Optional<DeskReservation> lockById(@Param("id") Long id);
}