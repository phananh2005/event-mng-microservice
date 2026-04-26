package com.sa.adminservice.repository;

import com.sa.adminservice.model.entity.Order;
import com.sa.adminservice.model.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT status AS status, COUNT(*) AS count " +
            "FROM events " +
            "WHERE YEAR(start_time) = :year AND QUARTER(start_time) = :quarter " +
            "GROUP BY status",
            nativeQuery = true)
    List<EventStatusStatsProjection> findEventStatusStats(
            @Param("quarter") Long quarter, @Param("year") Long year);

    @Query(value = """
            SELECT HOUR(e.start_time) AS hourOfDay,
                   COUNT(DISTINCT e.id) AS countEvents,
                   SUM(t.total_quantity) AS totalTickets,
                   SUM(t.total_quantity - t.remaining_quantity) AS ticketsSold,
                   CASE WHEN SUM(t.total_quantity) = 0 THEN 0
                        ELSE SUM(t.total_quantity - t.remaining_quantity) * 100.0 / SUM(t.total_quantity)
                   END AS percentageOfTicketsSold
            FROM events e
            JOIN ticket_types t ON e.id = t.event_id
            WHERE DAYOFWEEK(e.start_time) = :dayOfWeek
            GROUP BY HOUR(e.start_time)
            ORDER BY HOUR(e.start_time)
            """, nativeQuery = true)
    List<EventTemporalStatsProjection> findEventTemporalStats(@Param("dayOfWeek") Integer dayOfWeek);

    @Query(value = """
            SELECT e.name AS eventName,
                   SUM(oi.subtotal) AS totalRevenue,
                   SUM(tt.total_quantity - tt.remaining_quantity) AS ticketsSold,
                   CASE WHEN SUM(tt.total_quantity) = 0 THEN 0
                        ELSE SUM(tt.total_quantity - tt.remaining_quantity) * 100.0 / SUM(tt.total_quantity)
                   END AS percentageOfTicketsSold
            FROM orders o
            JOIN order_items oi ON o.id = oi.order_id
            JOIN ticket_types tt ON oi.ticket_type_id = tt.id
            RIGHT JOIN events e ON e.id = tt.event_id
            WHERE e.organizer_id = :id
            GROUP BY e.id
            """, nativeQuery = true)
    List<EventRevenueStatsOrganizerProjection> findEventRevenueOrganizerStats(@Param("id") Long id);

    @Query(value = "SELECT SUM(service_fee) AS totalRevenue FROM orders", nativeQuery = true)
    EventRevenueStatsAdminProjection findEventRevenueAdminStats();

    @Query(value = """
            SELECT YEAR(created_at) AS year, MONTH(created_at) AS month, SUM(service_fee) AS revenue
            FROM orders
            WHERE payment_status = 'PAID'
            GROUP BY YEAR(created_at), MONTH(created_at)
            ORDER BY year DESC, month DESC
            """, nativeQuery = true)
    List<MonthlyRevenueProjection> findMonthlyRevenueAdmin();
}
