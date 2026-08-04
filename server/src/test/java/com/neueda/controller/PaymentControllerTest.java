package com.neueda.controller;

import com.neueda.exception.DuplicatePaymentException;
import com.neueda.exception.ValidationException;
import com.neueda.model.ErrorCode;
import com.neueda.model.Payment;
import com.neueda.model.PaymentStatus;
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
    private StubPaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new StubPaymentService();
        mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void createPaymentReturnsCreatedPayment() throws Exception {
        paymentService.createdPayment = payment(99L, "CREATED", "idem-001");

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPaymentJson("idem-001")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.sourceAccount").value("ACC00111"))
            .andExpect(jsonPath("$.destinationAccount").value("ACC00222"))
            .andExpect(jsonPath("$.idempotencyKey").value("idem-001"));
    }

    @Test
    void getPaymentByIdReturnsPaymentWhenPresent() throws Exception {
        paymentService.paymentById = Optional.of(payment(99L, "CREATED", "idem-001"));

        mockMvc.perform(get("/payments/99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.sourceAccount").value("ACC00111"))
            .andExpect(jsonPath("$.destinationAccount").value("ACC00222"));
    }

    @Test
    void getPaymentByIdReturnsNotFoundWhenMissing() throws Exception {
        paymentService.paymentById = Optional.empty();

        mockMvc.perform(get("/payments/1000"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createPaymentReturnsValidationErrorWhenServiceRejectsRequest() throws Exception {
        paymentService.createException = new ValidationException(ErrorCode.INVALID_AMOUNT, "Amount must be greater than 0");

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPaymentJson("idem-invalid")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INVALID_AMOUNT"))
            .andExpect(jsonPath("$.httpStatus").value(400))
            .andExpect(jsonPath("$.details").value("Amount must be greater than 0"));
    }

    @Test
    void createPaymentReturnsConflictWhenDuplicatePaymentDetected() throws Exception {
        paymentService.createException = new DuplicatePaymentException("idem-duplicate", 101L);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPaymentJson("idem-duplicate")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("DUPLICATE_PAYMENT"))
            .andExpect(jsonPath("$.httpStatus").value(409))
            .andExpect(jsonPath("$.details").value("Idempotency key 'idem-duplicate' already exists with Payment ID 101"));
    }

    @Test
    void createPaymentReturnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": }"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentByIdReturnsInternalServerErrorForUnexpectedException() throws Exception {
        paymentService.readException = new RuntimeException("database unavailable");

        mockMvc.perform(get("/payments/55"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorCode").value("PROCESSING_ERROR"))
            .andExpect(jsonPath("$.httpStatus").value(500))
            .andExpect(jsonPath("$.details").value("database unavailable"));
    }

    private static Payment payment(Long id, String status, String key) {
        return new Payment(id, new BigDecimal("125.50"), status, "ACC00111", "ACC00222", key);
    }

    private static String validPaymentJson(String key) {
        return """
            {
              "amount": 125.50,
              "status": "CREATED",
              "sourceAccount": "ACC00111",
              "destinationAccount": "ACC00222",
              "idempotencyKey": "%s"
            }
            """.formatted(key);
    }

    private static class StubPaymentService implements PaymentService {
        private Payment createdPayment;
        private Optional<Payment> paymentById = Optional.empty();
        private RuntimeException createException;
        private RuntimeException readException;

        @Override
        public Payment createPayment(Payment payment) {
            if (createException != null) {
                throw createException;
            }
            return createdPayment;
        }

        @Override
        public Optional<Payment> getPaymentById(Long id) {
            if (readException != null) {
                throw readException;
            }
            return paymentById;
        }

        @Override
        public Payment transitionStatus(Long id, PaymentStatus targetStatus) {
            throw new UnsupportedOperationException("transitionStatus is not used in PaymentControllerTest");
        }

        @Override
        public List<Payment> getAllPayments() {
            return List.of();
        }

        @Override
        public Optional<Payment> getPaymentByIdempotencyKey(String key) {
            return Optional.empty();
        }
    }
}


