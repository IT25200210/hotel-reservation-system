package com.hotel.reservation.repository;

import com.hotel.reservation.entity.DeskStay;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface DeskStayRepository extends JpaRepository<DeskStay, Long> {
    List<DeskStay> findAllByOrderByIdDesc();

    boolean existsByRoomIdAndCheckedOutFalseAndArrivalLessThanAndDepartureGreaterThan(
            Long roomId, java.time.LocalDate end, java.time.LocalDate start);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select x from DeskStay x where x.id = :id")
    Optional<DeskStay> lockById(@Param("id") Long id);
}