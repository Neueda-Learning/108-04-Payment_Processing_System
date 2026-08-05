package com.neueda.controller;

import com.neueda.dto.PaymentStatsResponse;
import com.neueda.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

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
                20.0
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

    private static class StubPaymentService implements PaymentService {
        private PaymentStatsResponse stats;

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
        public com.neueda.model.Payment failPayment(Long paymentId, String errorCode, String technicalReason) {
            throw new UnsupportedOperationException();
        }
    }
}