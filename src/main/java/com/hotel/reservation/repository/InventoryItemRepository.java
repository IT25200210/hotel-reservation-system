package com.hotel.reservation.repository;
import com.hotel.reservation.entity.InventoryItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface InventoryItemRepository extends JpaRepository<InventoryItem,Long> {
 List<InventoryItem> findAllByOrderByNameAsc();
 boolean existsByCode(String code);
 boolean existsByCodeAndIdNot(String code,Long id);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select i from InventoryItem i where i.id=:id")
 Optional<InventoryItem> lockById(@Param("id") Long id);
}
