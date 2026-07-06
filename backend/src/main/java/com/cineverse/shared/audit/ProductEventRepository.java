package com.cineverse.shared.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductEventRepository extends JpaRepository<ProductEvent, Long> {

    @Query(value = "SELECT COUNT(DISTINCT profile_id) FROM product_events WHERE created_at::date = CURRENT_DATE",
           nativeQuery = true)
    long dailyActiveProfiles();
}
