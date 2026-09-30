package com.hotel.reservation.repository;

import com.hotel.reservation.entity.DeskRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeskRoomRepository extends JpaRepository<DeskRoom, Long> {

    List<DeskRoom> findAllByOrderByNumberAsc();

    List<DeskRoom> findByActiveTrueOrderByNumberAsc();

    boolean existsByNumber(String number);

    boolean existsByNumberAndIdNot(String number, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select x from DeskRoom x where x.id = :id")
    Optional<DeskRoom> lockById(@Param("id") Long id);
}