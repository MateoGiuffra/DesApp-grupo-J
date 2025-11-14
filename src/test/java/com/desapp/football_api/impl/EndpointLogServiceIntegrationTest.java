package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.services.EndpointLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Tag("integration")
class EndpointLogServiceIntegrationTest {
    @Autowired
    private EndpointLogService endpointLogService;
    private Long userId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userId = 1L;
        pageable = Pageable.unpaged();
    }

    @Test
    void testFindAllByUserIdAndDateRangeNullDates() {
        assertThrows(BadRequestException.class, () -> endpointLogService.findAllByUserIdAndDateRange(userId, null, null, pageable));
    }

    @Test
    void testFindAllByUserIdAndDateRangeInvalidDateRange() {
        LocalDate startInvalidDate = LocalDate.ofYearDay(2, 2);
        LocalDate endInvalidDate = LocalDate.ofYearDay(1, 1);
        assertThrows(BadRequestException.class, () -> endpointLogService.findAllByUserIdAndDateRange(userId, startInvalidDate, endInvalidDate, pageable));
    }


}
