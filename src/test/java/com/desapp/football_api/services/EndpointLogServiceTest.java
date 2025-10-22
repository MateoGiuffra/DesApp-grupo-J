package com.desapp.football_api.services;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.repository.EndpointLogRepository;
import com.desapp.football_api.service.EndpointLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndpointLogServiceTest {

    @Mock
    private EndpointLogRepository endpointLogRepository;

    @InjectMocks
    private EndpointLogService endpointLogService;

    private EndpointLog createLog(Long userId, LocalDate date) {
        EndpointLog log = new EndpointLog();
        log.setUserId(userId);
        log.setRequestPath("/api/test");
        log.setHttpMethod("GET");
        log.setStatusCode(200);
        log.setResponseTime("42");
        log.setRequestIp("127.0.0.1");
        log.setTimestamp(date);
        return log;
    }

    @Test
    void findAllByUserIdAndDateRange_inclusiveBoundaries_multipleMatches() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 10);
        LocalDate endDate = LocalDate.of(2025, 1, 20);
        Pageable pageable = PageRequest.of(0, 10);

        List<EndpointLog> logs = new ArrayList<>();
        logs.add(createLog(userId, LocalDate.of(2025, 1, 10)));
        logs.add(createLog(userId, LocalDate.of(2025, 1, 15)));
        logs.add(createLog(userId, LocalDate.of(2025, 1, 20)));
        Page<EndpointLog> pagedResponse = new PageImpl<>(logs, pageable, logs.size());

        when(endpointLogRepository.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable))
                .thenReturn(pagedResponse);

        Page<EndpointLog> result = endpointLogService.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable);

        assertEquals(3, result.getTotalElements(), "Should return 3 logs within inclusive range for user 1");
        assertTrue(result.getContent().stream().allMatch(l -> l.getUserId().equals(userId)));
        assertTrue(result.getContent().stream().map(EndpointLog::getTimestamp).allMatch(d ->
                !d.isBefore(startDate) && !d.isAfter(endDate)
        ));
    }

    @Test
    void findAllByUserIdAndDateRange_partialWindow_onlyMiddleMatch() {
        Long userId = 5L;
        LocalDate startDate = LocalDate.of(2025, 3, 5);
        LocalDate endDate = LocalDate.of(2025, 3, 15);
        Pageable pageable = PageRequest.of(0, 10);

        EndpointLog log = createLog(userId, LocalDate.of(2025, 3, 10));
        List<EndpointLog> logs = Collections.singletonList(log);
        Page<EndpointLog> pagedResponse = new PageImpl<>(logs, pageable, logs.size());

        when(endpointLogRepository.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable))
                .thenReturn(pagedResponse);

        Page<EndpointLog> result = endpointLogService.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable);

        assertEquals(1, result.getTotalElements());
        EndpointLog only = result.getContent().getFirst();
        assertEquals(userId, only.getUserId());
        assertEquals(LocalDate.of(2025, 3, 10), only.getTimestamp());
    }

    @Test
    void findAllByUserIdAndDateRange_excludesOutsideWindow() {
        Long userId = 7L;
        LocalDate startDate = LocalDate.of(2025, 4, 1);
        LocalDate endDate = LocalDate.of(2025, 4, 30);
        Pageable pageable = PageRequest.of(0, 10);

        Page<EndpointLog> pagedResponse = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(endpointLogRepository.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable))
                .thenReturn(pagedResponse);

        Page<EndpointLog> result = endpointLogService.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByUserIdAndDateRange_throwsOnInvalidParams() {
        Long userId = 9L;
        LocalDate start = LocalDate.of(2025, 6, 10);
        LocalDate end = LocalDate.of(2025, 6, 5); // end before start
        Pageable pageable = PageRequest.of(0, 10);

        // Null userId
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(null, start, start, pageable)
        );

        // Null dates
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(userId, null, end, pageable)
        );
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(userId, start, null, pageable)
        );

        // End before start
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(userId, start, end, pageable)
        );
    }

    @Test
    void save_callsRepository() {
        EndpointLog log = createLog(1L, LocalDate.now());
        when(endpointLogRepository.save(any(EndpointLog.class))).thenReturn(log);

        EndpointLog savedLog = endpointLogService.save(log);

        assertNotNull(savedLog);
        assertEquals(log.getUserId(), savedLog.getUserId());
    }
}
