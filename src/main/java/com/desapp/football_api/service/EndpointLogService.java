package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.repository.EndpointLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class EndpointLogService {
    private final EndpointLogRepository endpointLogRepository;

    public EndpointLog save(EndpointLog endpointLog) {
        return endpointLogRepository.save(endpointLog);
    }

    public List<EndpointLog> findAllByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null) {
            throw new BadRequestException("userId is null");
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BadRequestException("Invalid date range");
        }
        return endpointLogRepository.findAllByUserIdAndDateRange(userId, startDate, endDate);
    }

    public List<EndpointLog> findAll() {
        return endpointLogRepository.findAll();
    }

    public void deleteAll() {
        endpointLogRepository.deleteAll();
    }
}
