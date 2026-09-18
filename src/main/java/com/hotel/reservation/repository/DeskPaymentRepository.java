package com.hotel.reservation.repository;

import com.hotel.reservation.entity.DeskPayment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface DeskPaymentRepository extends JpaRepository<DeskPayment, Long> {
    Optional<DeskPayment> findByRequestKey(String requestKey);
    List<DeskPayment> findTop100ByOrderByIdDesc();
}
