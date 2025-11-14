package com.desapp.football_api.services.impl;

import com.desapp.football_api.aspects.NonCacheable;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.repository.EndpointLogRepository;
import com.desapp.football_api.services.EndpointLogService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@AllArgsConstructor
@NonCacheable
public class EndpointLogServiceImpl implements EndpointLogService {
    private final EndpointLogRepository endpointLogRepository;

    @Override
    public EndpointLog save(EndpointLog endpointLog) {
        return endpointLogRepository.save(endpointLog);
    }

    @Override
    public Page<EndpointLog> findAllByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        validateFindAllByUserIdAndDateRange(userId, startDate, endDate);
        return endpointLogRepository.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable);
    }

    private void validateFindAllByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null) {
            throw new BadRequestException("UserId is null");
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BadRequestException("Invalid date range");
        }
    }

}


