package com.xinchao.FoodApp.payment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xinchao.FoodApp.auth_users.dtos.UserDTO;
import com.xinchao.FoodApp.enums.PaymentGateway;
import com.xinchao.FoodApp.enums.PaymentStatus;
import com.xinchao.FoodApp.order.dtos.OrderDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {
    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private String transactionId;

    private PaymentGateway paymentGateway;

    private String failureReasons;

    private boolean success;

    private LocalDateTime paymentDate;

    private OrderDTO order;

    private UserDTO user;
}
