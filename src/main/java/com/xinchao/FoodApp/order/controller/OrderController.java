package com.xinchao.FoodApp.order.controller;

import com.xinchao.FoodApp.enums.OrderStatus;
import com.xinchao.FoodApp.order.dtos.OrderDTO;
import com.xinchao.FoodApp.order.dtos.OrderItemDTO;
import com.xinchao.FoodApp.order.service.OrderService;
import com.xinchao.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Response<?>> placeOrderFromCart(){
        return ResponseEntity.ok(orderService.placeOrderFromCart());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Response<OrderDTO>> getOrderById(@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<Page<OrderDTO>>> getAllOrders(@RequestParam(required = false) OrderStatus orderStatus,
                                                                 @RequestParam(defaultValue = "0")  int page,
                                                                 @RequestParam(defaultValue = "100") int size){

        return ResponseEntity.ok(orderService.getAllOrders(orderStatus, page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<Response<List<OrderDTO>>> getOrdersOfUser(){
        return ResponseEntity.ok(orderService.getOrdersOfUser());
    }

    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<Response<OrderItemDTO>> getOrderItemById(@PathVariable Long orderItemId){
        return ResponseEntity.ok(orderService.getOrderItemById(orderItemId));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<OrderDTO>> updateOrderStatus(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.updateOrderStatus(orderDTO));
    }

    @GetMapping("/unique-customers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<?>> countUniqueCustomers(){
        return ResponseEntity.ok(orderService.countUniqueCustomers());
    }
}

