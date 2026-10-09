package com.hotel.reservation.repository;
import com.hotel.reservation.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.Optional;
public interface StockMovementRepository extends JpaRepository<StockMovement,Long> {
 boolean existsByItemId(Long id);
 Optional<StockMovement> findByRequestKey(String key);
 Page<StockMovement> findByItemId(Long id,Pageable pageable);
}
