package com.desapp.football_api.services;

import com.desapp.football_api.model.EndpointLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface EndpointLogService {
    EndpointLog save(EndpointLog endpointLog);

    Page<EndpointLog> findAllByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<EndpointLog> findAll();

    void deleteAll();
}
