package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.response.DailyRevenueResponse;
import com.quickbite.backend.dto.response.ReportResponse;
import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.repository.OrderItemRepository;
import com.quickbite.backend.repository.OrderRepository;
import com.quickbite.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final int TOP_N = 5;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReport(int days) {
        int range = Math.min(Math.max(days, 1), 30);
        Pageable topFive = PageRequest.of(0, TOP_N);

        // Every status appears in the result, even with zero orders
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status.name(), 0L);
        }
        for (Object[] row : orderRepository.statusCounts()) {
            byStatus.put(((OrderStatus) row[0]).name(), (Long) row[1]);
        }

        // Daily revenue, zero-filled so the chart has no gaps. Grouping is done in Java because the
        // date-truncation syntax differs between database dialects; at this scale it is a small result.
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(range - 1L);
        Map<LocalDate, BigDecimal> revenue = new LinkedHashMap<>();
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < range; i++) {
            LocalDate day = from.plusDays(i);
            revenue.put(day, BigDecimal.ZERO);
            counts.put(day, 0L);
        }
        for (Object[] row : orderRepository.deliveredSince(from.atStartOfDay())) {
            LocalDate day = ((LocalDateTime) row[0]).toLocalDate();
            revenue.merge(day, (BigDecimal) row[1], BigDecimal::add);
            counts.merge(day, 1L, Long::sum);
        }
        List<DailyRevenueResponse> daily = revenue.entrySet().stream()
                .map(e -> new DailyRevenueResponse(e.getKey(), e.getValue(), counts.get(e.getKey())))
                .toList();

        return ReportResponse.builder()
                .rangeDays(range)
                .topRestaurants(orderRepository.topRestaurants(topFive))
                .popularFoods(orderItemRepository.popularFoods(topFive))
                .ordersByStatus(byStatus)
                .dailyRevenue(daily)
                .build();
    }
}