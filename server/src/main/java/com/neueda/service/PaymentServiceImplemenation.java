package com.neueda.service;

import com.neueda.model.Payment;
import com.neueda.repository.PaymentRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class PaymentServiceImplemenation implements PaymentService {


    private final PaymentRepository repository;


    public PaymentServiceImplemenation(PaymentRepository repository) {
        this.repository = repository;
    }



    @Override
    public Payment createPayment(Payment payment) {

        /*
         Later:
         - validate amount
         - check account exists
         - check duplicate payment
         - set initial status

         For now:
         just save
        */

        return repository.save(payment);
    }



    @Override
    public Optional<Payment> getPaymentById(Long id) {

        return repository.findById(id);

    }



    @Override
    public List<Payment> getAllPayments() {

        return repository.findAll();

    }



    @Override
    public Optional<Payment> getPaymentByIdempotencyKey(String key) {

        return repository.findByIdempotencyKey(key);

    }

}
