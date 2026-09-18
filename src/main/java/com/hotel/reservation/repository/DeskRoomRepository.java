package com.hotel.reservation.repository;

import com.hotel.reservation.entity.DeskRoom;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface DeskRoomRepository extends JpaRepository<DeskRoom, Long> {
    List<DeskRoom> findAllByOrderByNumberAsc();

    boolean existsByNumber(String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select x from DeskRoom x where x.id = :id")
    Optional<DeskRoom> lockById(@Param("id") Long id);
}
