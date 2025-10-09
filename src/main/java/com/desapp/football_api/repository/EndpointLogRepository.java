package com.desapp.football_api.repository;

import com.desapp.football_api.model.EndpointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EndpointLogRepository extends JpaRepository<EndpointLog, Long> {
    @Query("""
            SELECT e 
            FROM EndpointLog e 
            WHERE e.userId = :userId 
              AND e.timestamp BETWEEN :startDate AND :endDate
            """)
    List<EndpointLog> findAllByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
