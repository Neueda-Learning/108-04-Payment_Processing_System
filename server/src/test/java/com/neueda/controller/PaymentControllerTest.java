package com.neueda.controller;

import com.neueda.model.Payment;
import com.neueda.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentService paymentService = new PaymentService() {
            @Override
            public Payment createPayment(Payment payment) {
                payment.setId(99L);
                if (payment.getStatus() == null) {
                    payment.setStatus("CREATED");
                }
                return payment;
            }

            @Override
            public Optional<Payment> getPaymentById(Long id) {
                if (id.equals(99L)) {
                    return Optional.of(new Payment(
                            99L,
                            new BigDecimal("125.50"),
                            "CREATED",
                            "ACC001",
                            "ACC002",
                            "idem-001"
                    ));
                }
                return Optional.empty();
            }

            @Override
            public List<Payment> getAllPayments() {
                return List.of();
            }

            @Override
            public Optional<Payment> getPaymentByIdempotencyKey(String key) {
                return Optional.empty();
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService)).build();
    }

    @Test
    void createPaymentReturnsCreatedPayment() throws Exception {
        String paymentJson = """
                {
                  "amount": 125.50,
                  "sourceAccount": "ACC001",
                  "destinationAccount": "ACC002",
                  "idempotencyKey": "idem-001"
                }
                """;

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.sourceAccount").value("ACC001"))
                .andExpect(jsonPath("$.destinationAccount").value("ACC002"))
                .andExpect(jsonPath("$.idempotencyKey").value("idem-001"));
    }

    @Test
    void getPaymentByIdReturnsPaymentWhenPresent() throws Exception {
        mockMvc.perform(get("/payments/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.sourceAccount").value("ACC001"))
                .andExpect(jsonPath("$.destinationAccount").value("ACC002"));
    }

    @Test
    void getPaymentByIdReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get("/payments/1000"))
                .andExpect(status().isNotFound());
    }
}


