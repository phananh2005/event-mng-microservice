package com.sa.adminservice.model.projection;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {
    int getYear();
    int getMonth();
    BigDecimal getRevenue();
}
