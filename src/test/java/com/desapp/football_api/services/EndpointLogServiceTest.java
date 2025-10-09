package com.desapp.football_api.services;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.repository.EndpointLogRepository;
import com.desapp.football_api.service.EndpointLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Tag("unit")
class EndpointLogServiceTest {

    @Autowired
    private EndpointLogService endpointLogService;

    @Autowired
    private EndpointLogRepository endpointLogRepository;

    @Test
    void findAllByUserIdAndDateRange_inclusiveBoundaries_multipleMatches() {
        Long userId = 1L;
        // Given: three logs for the same user at 10th, 15th and 20th of January 2025
        saveLog(userId, LocalDate.of(2025, 1, 10));
        saveLog(userId, LocalDate.of(2025, 1, 15));
        saveLog(userId, LocalDate.of(2025, 1, 20));
        // And: some noise for another user and outside the range
        saveLog(2L, LocalDate.of(2025, 1, 12));
        saveLog(userId, LocalDate.of(2024, 12, 31));
        saveLog(userId, LocalDate.of(2025, 2, 1));

        // When: we search from 10 to 20 inclusive
        List<EndpointLog> result = endpointLogService.findAllByUserIdAndDateRange(
                userId,
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 20)
        );

        // Then: all three boundary-inclusive items are returned
        assertEquals(3, result.size(), "Should return 3 logs within inclusive range for user 1");
        assertTrue(result.stream().allMatch(l -> l.getUserId().equals(userId)));
        assertTrue(result.stream().map(EndpointLog::getTimestamp).allMatch(d ->
                !d.isBefore(LocalDate.of(2025, 1, 10)) && !d.isAfter(LocalDate.of(2025, 1, 20))
        ));
    }

    @Test
    void findAllByUserIdAndDateRange_partialWindow_onlyMiddleMatch() {
        Long userId = 5L;
        // Given
        saveLog(userId, LocalDate.of(2025, 3, 1));
        saveLog(userId, LocalDate.of(2025, 3, 10));
        saveLog(userId, LocalDate.of(2025, 3, 25));
        // Another user's logs overlapping the window should be ignored
        saveLog(6L, LocalDate.of(2025, 3, 10));

        // When: window 5..15 picks only the middle one for user 5
        List<EndpointLog> result = endpointLogService.findAllByUserIdAndDateRange(
                userId,
                LocalDate.of(2025, 3, 5),
                LocalDate.of(2025, 3, 15)
        );

        // Then
        assertEquals(1, result.size());
        EndpointLog only = result.get(0);
        assertEquals(userId, only.getUserId());
        assertEquals(LocalDate.of(2025, 3, 10), only.getTimestamp());
    }

    @Test
    void findAllByUserIdAndDateRange_excludesOutsideWindow() {
        Long userId = 7L;
        // Given: all logs exist but all are outside the window
        saveLog(userId, LocalDate.of(2025, 5, 1));
        saveLog(userId, LocalDate.of(2025, 5, 2));
        saveLog(userId, LocalDate.of(2025, 5, 3));

        // When: a window in April
        List<EndpointLog> result = endpointLogService.findAllByUserIdAndDateRange(
                userId,
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 30)
        );

        // Then: nothing matches
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByUserIdAndDateRange_throwsOnInvalidParams() {
        Long userId = 9L;
        LocalDate start = LocalDate.of(2025, 6, 10);
        LocalDate end = LocalDate.of(2025, 6, 5); // end before start

        // Null userId
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(null, start, start)
        );

        // Null dates
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(userId, null, end)
        );
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(userId, start, null)
        );

        // End before start
        assertThrows(BadRequestException.class, () ->
                endpointLogService.findAllByUserIdAndDateRange(userId, start, end)
        );
    }

    private void saveLog(Long userId, LocalDate date) {
        EndpointLog log = new EndpointLog();
        log.setUserId(userId);
        log.setRequestPath("/api/test");
        log.setHttpMethod("GET");
        log.setStatusCode(200);
        log.setResponseContentLength(123L);
        log.setResponseTime(42L);
        log.setRequestIp("127.0.0.1");
        log.setTimestamp(date);
        endpointLogService.save(log);
    }
}
