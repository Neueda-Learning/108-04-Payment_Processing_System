package com.neueda.controller;

import com.neueda.dto.PaymentStatsResponse;
import com.neueda.dto.DashboardStatsResponse;
import com.neueda.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final PaymentService paymentService;

    public StatsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments")
    public ResponseEntity<PaymentStatsResponse> getPaymentStats() {
        return ResponseEntity.ok(paymentService.getPaymentStats());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(paymentService.getDashboardStats(from, to));
    }
}