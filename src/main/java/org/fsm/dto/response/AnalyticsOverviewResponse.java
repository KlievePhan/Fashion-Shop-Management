package org.fsm.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsOverviewResponse {

    private BigDecimal totalRevenue;
    private long totalOrders;

    private long completedOrders;
    private long pendingOrders;
    private long cancelledOrders;

    private List<SalesPoint> salesByDate;
    private List<CategoryDistributionItem> categoryDistribution;

    @Data
    @Builder
    public static class SalesPoint {
        private LocalDate date;
        private BigDecimal revenue;
        private long orders;
    }

    @Data
    @Builder
    public static class CategoryDistributionItem {
        private String categoryName;
        private long quantity;
        private BigDecimal revenue;
        private double percent;
    }
}


