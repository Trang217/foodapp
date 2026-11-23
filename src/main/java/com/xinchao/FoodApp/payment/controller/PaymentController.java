package com.xinchao.FoodApp.payment.controller;

import com.xinchao.FoodApp.payment.dtos.PaymentDTO;
import com.xinchao.FoodApp.payment.service.PaymentService;
import com.xinchao.FoodApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<Response<?>>inititalizePayment(@RequestBody @Valid PaymentDTO paymentDTO){
        return ResponseEntity.ok(paymentService.inititalizePayment(paymentDTO));
    }


    @PutMapping("/update")
    public void updatePaymentForOrder(@RequestBody PaymentDTO paymentDTO){
         paymentService.updatePaymentForOrder(paymentDTO);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<PaymentDTO>>>getAllPayments(){
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Response<PaymentDTO>> getPaymentById(@PathVariable Long paymentId){
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }
}
