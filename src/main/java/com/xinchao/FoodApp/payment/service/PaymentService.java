package com.xinchao.FoodApp.payment.service;

import com.xinchao.FoodApp.payment.dtos.PaymentDTO;
import com.xinchao.FoodApp.response.Response;

import java.util.List;

public interface PaymentService {
    Response<?> inititalizePayment(PaymentDTO paymentDTO);
    void updatePaymentForOrder(PaymentDTO paymentDTO);
    Response<List<PaymentDTO>> getAllPayments();
    Response<PaymentDTO> getPaymentById(Long paymentId);
}
