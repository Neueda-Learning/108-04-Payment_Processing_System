package com.neueda.controller;

import com.neueda.exception.DuplicatePaymentException;
import com.neueda.exception.InvalidStatusTransitionException;
import com.neueda.exception.PaymentNotFoundException;
import com.neueda.exception.ValidationException;
import com.neueda.model.ErrorCode;
import com.neueda.model.PaymentHistory;
import com.neueda.model.Payment;
import com.neueda.model.PaymentStatus;
import com.neueda.dto.UpdatePaymentStatusRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void getPaymentsReturnsAllPaymentsWhenNoStatusIsProvided() throws Exception {
        paymentService.allPayments = List.of(
            payment(1L, "CREATED", "idem-1"),
            payment(2L, "VALIDATED", "idem-2")
        );

        mockMvc.perform(get("/payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getPaymentsReturnsFilteredPaymentsWhenStatusIsProvided() throws Exception {
        paymentService.paymentsByStatus = List.of(payment(2L, "VALIDATED", "idem-2"));

        mockMvc.perform(get("/payments").param("status", "validated"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("VALIDATED"));
    }

    @Test
    void getPaymentsReturnsBadRequestForInvalidStatus() throws Exception {
        paymentService.statusException = new ValidationException(ErrorCode.VALIDATION_FAILED, "Unsupported payment status: UNKNOWN");

        mockMvc.perform(get("/payments").param("status", "UNKNOWN"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.httpStatus").value(400));
    }

    @Test
    void updatePaymentStatusReturnsUpdatedPayment() throws Exception {
        paymentService.transitionResult = payment(99L, "VALIDATED", "idem-001");

        mockMvc.perform(put("/payments/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"VALIDATED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    void updatePaymentStatusReturnsBadRequestForUnsupportedStatus() throws Exception {
        mockMvc.perform(put("/payments/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"UNKNOWN\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.httpStatus").value(400));
    }

    @Test
    void updatePaymentStatusReturnsNotFoundWhenPaymentIsMissing() throws Exception {
        paymentService.statusException = new PaymentNotFoundException(99L);

        mockMvc.perform(put("/payments/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"VALIDATED\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"))
            .andExpect(jsonPath("$.httpStatus").value(404));
    }

    @Test
    void updatePaymentStatusReturnsBadRequestForInvalidTransition() throws Exception {
        paymentService.statusException = new InvalidStatusTransitionException(PaymentStatus.CREATED, PaymentStatus.COMPLETED);

        mockMvc.perform(put("/payments/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INVALID_STATUS_TRANSITION"))
            .andExpect(jsonPath("$.httpStatus").value(400));
    }

    @Test
    void getPaymentHistoryReturnsHistoryEntries() throws Exception {
        paymentService.paymentHistory = List.of(
            new PaymentHistory(10L, 99L, null, "CREATED", null, "Created"),
            new PaymentHistory(11L, 99L, "CREATED", "VALIDATED", null, "Validated")
        );

        mockMvc.perform(get("/payments/99/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].toStatus").value("CREATED"))
            .andExpect(jsonPath("$[1].fromStatus").value("CREATED"))
            .andExpect(jsonPath("$[1].toStatus").value("VALIDATED"));
    }

    @Test
    void getPaymentHistoryReturnsNotFoundWhenPaymentIsMissing() throws Exception {
        paymentService.paymentHistoryException = new PaymentNotFoundException(99L);

        mockMvc.perform(get("/payments/99/history"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"))
            .andExpect(jsonPath("$.httpStatus").value(404));
    }

    @Test
    void getPaymentByIdempotencyKeyReturnsPaymentWhenPresent() throws Exception {
        paymentService.paymentByIdempotencyKey = Optional.of(payment(99L, "CREATED", "idem-001"));

        mockMvc.perform(get("/payments/idempotency/idem-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.idempotencyKey").value("idem-001"));
    }

    @Test
    void getPaymentByIdempotencyKeyReturnsNotFoundWhenMissing() throws Exception {
        paymentService.paymentByIdempotencyKey = Optional.empty();

        mockMvc.perform(get("/payments/idempotency/idem-missing"))
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
    return new Payment(
            id,
            new BigDecimal("125.50"),
            status,
            "ACC00111",
            "ACC00222",
            key,
            "Test payment"
    );
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
        private Optional<Payment> paymentByIdempotencyKey = Optional.empty();
        private Payment transitionResult;
        private List<Payment> allPayments = List.of();
        private List<Payment> paymentsByStatus = List.of();
        private List<PaymentHistory> paymentHistory = List.of();
        private RuntimeException createException;
        private RuntimeException readException;
        private RuntimeException statusException;
        private RuntimeException paymentHistoryException;

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
        public List<PaymentHistory> getPaymentHistory(Long id) {
            if (paymentHistoryException != null) {
                throw paymentHistoryException;
            }
            return paymentHistory;
        }

        @Override
        public List<Payment> getPaymentsByStatus(String status) {
            if (statusException != null) {
                throw statusException;
            }
            return paymentsByStatus;
        }

        @Override
        public Payment transitionStatus(Long id, PaymentStatus targetStatus) {
            if (statusException != null) {
                throw statusException;
            }
            return transitionResult;
        }

        @Override
        public List<Payment> getAllPayments() {
            return allPayments;
        }

        @Override
        public Optional<Payment> getPaymentByIdempotencyKey(String key) {
            return paymentByIdempotencyKey;
        }

        @Override
        public com.neueda.dto.PaymentStatsResponse getPaymentStats() {
            throw new UnsupportedOperationException("getPaymentStats is not used in PaymentControllerTest");
        }

        @Override
        public Payment failPayment(Long paymentId, String errorCode, String technicalReason) {
            throw new UnsupportedOperationException("failPayment is not used in PaymentControllerTest");
        }
    }
}


