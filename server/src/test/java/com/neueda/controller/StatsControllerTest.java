package com.neueda.controller;

import com.neueda.dto.PaymentStatsResponse;
import com.neueda.dto.DashboardStatsResponse;
import com.neueda.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatsControllerTest {

    private MockMvc mockMvc;
    private StubPaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new StubPaymentService();
        mockMvc = MockMvcBuilders.standaloneSetup(new StatsController(paymentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getPaymentStatsReturnsAggregates() throws Exception {
        paymentService.stats = new PaymentStatsResponse(
                5L,
                3L,
                1L,
                new BigDecimal("425.50"),
                60.0,
                20.0,
                new BigDecimal("85.10"),
                new BigDecimal("200.00")
        );

        mockMvc.perform(get("/stats/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayments").value(5))
                .andExpect(jsonPath("$.successfulPayments").value(3))
                .andExpect(jsonPath("$.failedPayments").value(1))
                .andExpect(jsonPath("$.totalAmount").value(425.50))
                .andExpect(jsonPath("$.successRate").value(60.0))
                .andExpect(jsonPath("$.failureRate").value(20.0));
    }

    @Test
    void getDashboardStatsReturnsAggregatedSections() throws Exception {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);
        paymentService.dashboardStats = new DashboardStatsResponse(
                from, to,
                List.of(new DashboardStatsResponse.StatusCount("COMPLETED", 2)),
                List.of(new DashboardStatsResponse.VolumePoint("2026-07-15", 2, new BigDecimal("150.00"))),
                List.of(new DashboardStatsResponse.FailureReasonCount("INSUFFICIENT_FUNDS", 1)),
                List.of(new DashboardStatsResponse.StageDuration("CREATED_TO_VALIDATED", 1.5)),
                List.of(new DashboardStatsResponse.SuccessRatePoint("2026-07-15", 100.0)),
                List.of(new DashboardStatsResponse.CurrencyBreakdown("USD", 2, new BigDecimal("150.00"))),
                List.of(new DashboardStatsResponse.AccountVolume("ACC1", 2, new BigDecimal("150.00"))),
                List.of(new DashboardStatsResponse.AccountVolume("ACC2", 2, new BigDecimal("150.00"))),
                90.0,
                List.of(new DashboardStatsResponse.HourlyVolume(14, 2, new BigDecimal("150.00")))
        );

        mockMvc.perform(get("/stats/dashboard").param("from", "2026-07-01").param("to", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2026-07-01"))
                .andExpect(jsonPath("$.to").value("2026-07-31"))
                .andExpect(jsonPath("$.statusDistribution[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.volumeOverTime[0].count").value(2))
                .andExpect(jsonPath("$.failureReasons[0].errorCode").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.avgStageDuration[0].avgSeconds").value(1.5))
                .andExpect(jsonPath("$.successRateOverTime[0].successRate").value(100.0))
                .andExpect(jsonPath("$.currencyBreakdown[0].currency").value("USD"));
    }

    @Test
    void getDashboardStatsWorksWithoutExplicitDateRange() throws Exception {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(29);
        paymentService.dashboardStats = new DashboardStatsResponse(
                from, to, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), 0.0, List.of());

        mockMvc.perform(get("/stats/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value(from.toString()))
                .andExpect(jsonPath("$.to").value(to.toString()));
    }

    private static class StubPaymentService implements PaymentService {
        private PaymentStatsResponse stats;
        private DashboardStatsResponse dashboardStats;

        @Override
        public com.neueda.model.Payment createPayment(com.neueda.model.Payment payment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public com.neueda.model.Payment transitionStatus(Long id, com.neueda.model.PaymentStatus targetStatus) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.Optional<com.neueda.model.Payment> getPaymentById(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.List<com.neueda.model.PaymentHistory> getPaymentHistory(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.List<com.neueda.model.Payment> getPaymentsByStatus(String status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.List<com.neueda.model.Payment> getAllPayments() {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.Optional<com.neueda.model.Payment> getPaymentByIdempotencyKey(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PaymentStatsResponse getPaymentStats() {
            return stats;
        }

        @Override
        public DashboardStatsResponse getDashboardStats(LocalDate from, LocalDate to) {
            return dashboardStats;
        }

        @Override
        public com.neueda.model.Payment failPayment(Long paymentId, String errorCode, String technicalReason) {
            throw new UnsupportedOperationException();
        }
    }
}