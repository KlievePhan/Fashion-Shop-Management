package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.fsm.dto.response.AnalyticsOverviewResponse;
import org.fsm.entity.Order;
import org.fsm.entity.OrderItem;
import org.fsm.entity.Product;
import org.fsm.repository.OrderItemRepository;
import org.fsm.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Tổng quan analytics cho admin
     *
     * @param days số ngày gần nhất để tính biểu đồ doanh thu (ví dụ 30)
     */
    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse getOverview(int days) {
        if (days <= 0) {
            days = 30;
        }

        // --- 1. Thống kê đơn hàng theo trạng thái ---
        long totalOrders = orderRepository.count();

        List<String> completedStatuses = List.of("PAID", "CONFIRMED", "DELIVERED");
        long completedOrders = completedStatuses.stream()
                .mapToLong(status -> orderRepository.countByStatus(status))
                .sum();

        long pendingOrders = orderRepository.countByStatus("PENDING")
                + orderRepository.countByStatus("COD_PENDING");
        long cancelledOrders = orderRepository.countByStatus("CANCELLED");

        // --- 2. Doanh thu & biểu đồ theo ngày cho các đơn đã hoàn tất ---

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1L);
        LocalDateTime fromDateTime = fromDate.atStartOfDay();

        List<Order> completedOrderList = orderRepository
                .findByCreatedAtAfterAndStatusIn(fromDateTime, completedStatuses);

        BigDecimal totalRevenue = completedOrderList.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Gom nhóm theo ngày
        Map<LocalDate, List<Order>> byDate = completedOrderList.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<AnalyticsOverviewResponse.SalesPoint> salesPoints = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = fromDate.plusDays(i);
            List<Order> ordersOfDay = byDate.getOrDefault(date, Collections.emptyList());

            BigDecimal revenueOfDay = ordersOfDay.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            salesPoints.add(AnalyticsOverviewResponse.SalesPoint.builder()
                    .date(date)
                    .revenue(revenueOfDay)
                    .orders(ordersOfDay.size())
                    .build());
        }

        // --- 3. Phân bố doanh thu theo category ---
        List<AnalyticsOverviewResponse.CategoryDistributionItem> categoryStats =
                buildCategoryDistribution(completedOrderList, totalRevenue);

        return AnalyticsOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .pendingOrders(pendingOrders)
                .cancelledOrders(cancelledOrders)
                .salesByDate(salesPoints)
                .categoryDistribution(categoryStats)
                .build();
    }

    private List<AnalyticsOverviewResponse.CategoryDistributionItem> buildCategoryDistribution(
            List<Order> completedOrders,
            BigDecimal totalRevenue
    ) {
        if (completedOrders.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderItem> items = orderItemRepository.findByOrderIn(completedOrders);

        Map<String, CategoryAccumulator> map = new HashMap<>();

        for (OrderItem item : items) {
            if (item == null || item.getSubtotal() == null) continue;

            Product product = item.getProduct();
            if (product == null || product.getCategory() == null) continue;

            String categoryName = product.getCategory().getName();
            if (categoryName == null) {
                categoryName = "Other";
            }

            CategoryAccumulator acc = map.computeIfAbsent(categoryName, k -> new CategoryAccumulator());
            acc.quantity += Optional.ofNullable(item.getQty()).orElse(0);
            acc.revenue = acc.revenue.add(item.getSubtotal());
        }

        if (map.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal denominator = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue
                : map.values().stream()
                .map(a -> a.revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return map.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().revenue.compareTo(e1.getValue().revenue))
                .map(e -> {
                    CategoryAccumulator acc = e.getValue();
                    double percent = denominator.compareTo(BigDecimal.ZERO) == 0
                            ? 0.0
                            : acc.revenue
                            .multiply(BigDecimal.valueOf(100))
                            .divide(denominator, 2, RoundingMode.HALF_UP)
                            .doubleValue();

                    return AnalyticsOverviewResponse.CategoryDistributionItem.builder()
                            .categoryName(e.getKey())
                            .quantity(acc.quantity)
                            .revenue(acc.revenue)
                            .percent(percent)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static class CategoryAccumulator {
        long quantity = 0L;
        BigDecimal revenue = BigDecimal.ZERO;
    }
}


