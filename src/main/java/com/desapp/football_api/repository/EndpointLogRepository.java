package com.desapp.football_api.repository;

import com.desapp.football_api.model.EndpointLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EndpointLogRepository extends JpaRepository<EndpointLog, Long> {
    @Query("""
            SELECT e 
            FROM EndpointLog e 
            WHERE e.userId = :userId 
              AND e.timestamp BETWEEN :startDate AND :endDate
            """)
    Page<EndpointLog> findAllByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
}
