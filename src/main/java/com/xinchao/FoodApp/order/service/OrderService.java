package com.xinchao.FoodApp.order.service;

import com.xinchao.FoodApp.enums.OrderStatus;
import com.xinchao.FoodApp.order.dtos.OrderDTO;
import com.xinchao.FoodApp.order.dtos.OrderItemDTO;
import com.xinchao.FoodApp.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?>placeOrderFromCart();

    Response<OrderDTO>getOrderById(Long orderId);

    Response<Page<OrderDTO>>getAllOrders(OrderStatus orderStatus, int page, int size);

    Response<List<OrderDTO>>getOrdersOfUser();

    Response<OrderItemDTO>getOrderItemById(Long orderItemId);

    Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO);

    Response<Long> countUniqueCustomers();
}
