package com.xinchao.FoodApp.order.service;

import com.xinchao.FoodApp.auth_users.entity.User;
import com.xinchao.FoodApp.auth_users.services.UserService;
import com.xinchao.FoodApp.cart.entity.Cart;
import com.xinchao.FoodApp.cart.entity.CartItem;
import com.xinchao.FoodApp.cart.repository.CartRepository;
import com.xinchao.FoodApp.cart.service.CartService;
import com.xinchao.FoodApp.email_notification.dtos.NotificationDTO;
import com.xinchao.FoodApp.email_notification.service.NotificationService;
import com.xinchao.FoodApp.enums.OrderStatus;
import com.xinchao.FoodApp.enums.PaymentStatus;
import com.xinchao.FoodApp.exceptions.BadRequestException;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.order.dtos.OrderDTO;
import com.xinchao.FoodApp.order.dtos.OrderItemDTO;
import com.xinchao.FoodApp.order.entity.Order;
import com.xinchao.FoodApp.order.entity.OrderItem;
import com.xinchao.FoodApp.order.repository.OrderItemRepository;
import com.xinchao.FoodApp.order.repository.OrderRepository;
import com.xinchao.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final UserService userService;

    @Value("${base.payment.link}")
    private String basePaymentLink;


    @Override
    public Response<?> placeOrderFromCart() {
        log.info(" inside placeOrderFromCart");
        User customer = userService.getCurrentLogInUser();
        String deliveryAddress = customer.getAddress();
        if (deliveryAddress == null) {
            throw new NotFoundException("Delivery address not present for user");
        }

        Cart cart = cartRepository.findByUser_Id(customer.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found"));

        List<CartItem> cartItems = cart.getCartItems();

        if(cartItems == null || cartItems.isEmpty()){
            throw new BadRequestException("cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for(CartItem item : cartItems){
            OrderItem orderItem =  OrderItem.builder()
                    .menu(item.getMenu())
                    .quantity(item.getQuantity())
                    .pricePerUnit(item.getPricePerUnit())
                    .subtotal(item.getSubTotal())
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        Order order = Order.builder()
                .user(customer)
                .orderItems(orderItems)
                .totalAmount(totalAmount)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.INITIALIZED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // save order and order item
        Order savedOrder = orderRepository.save(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);
        // clear cart
        cartService.clearShoppingCart();

        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        // send email notification
        sendOrderConfirmationEmail(customer, orderDTO);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your order has been received! We have sent a secure payment link to your email. Please proceed the payment to confirm your order!")
                .build();
    }

    @Override
    public Response<OrderDTO> getOrderById(Long orderId) {
        log.info(" inside getOrderById");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException("No order found"));
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .data(orderDTO)
                .message("get order by id successfully")
                .build();
    }

    @Override
    public Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size) {
        log.info(" inside getAllOrders");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orderPage;
        if (orderStatus != null){
            orderPage = orderRepository.findByOrderStatus(orderStatus, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }
        Page<OrderDTO> orderDTOPage = orderPage.map(
                order -> {
                    OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
                    orderDTO.getOrderItems().forEach(itemDto -> itemDto.getMenu().setReviews(null));
                    return orderDTO;
                }
        );

        return Response.<Page<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("get orders successfully")
                .data(orderDTOPage)
                .build();
    }

    @Override
    public Response<List<OrderDTO>> getOrdersOfUser() {
        log.info(" inside getOrdersOfUser");
        User customer = userService.getCurrentLogInUser();
        List<Order> orderList = orderRepository.findByUserOrderByOrderDateDesc(customer);
        List<OrderDTO> orderDTOList = orderList.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();

        orderDTOList.forEach(item -> {
            item.setUser(null);
            item.getOrderItems().forEach(el -> el.getMenu().setReviews(null));
        });

        return Response.<List<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(orderDTOList)
                .message("get orders successfully")
                .build();
    }

    @Override
    public Response<OrderItemDTO> getOrderItemById(Long orderItemId) {
        log.info(" inside getOrderItemById");

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(()-> new NotFoundException("order item not found"));

        OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);

        return Response.<OrderItemDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("get order by id successfully")
                .data(orderItemDTO)
                .build();
    }

    @Override
    public Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO) {
        log.info(" inside updateOrderStatus");
        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(()-> new NotFoundException("order not found"));

        order.setOrderStatus(orderDTO.getOrderStatus());
        orderRepository.save(order);

        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("update order successfully")
                .build();
    }

    @Override
    public Response<Long> countUniqueCustomers() {
        long uniqueCustomerCount = orderRepository.countDistinctUsers();

        return Response.<Long>builder()
                .statusCode(HttpStatus.OK.value())
                .message("unique customer count successfully")
                .data(uniqueCustomerCount)
                .build();
    }

    private void sendOrderConfirmationEmail(User customer, OrderDTO orderDTO){
        String subject = "Your Order Confirmation - Order #" + orderDTO.getId();
        // create thymlef context and set variables, import the context from thymleaf
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", customer.getName());
        context.setVariable("orderId", String.valueOf(orderDTO.getId()));
        context.setVariable("orderDate", orderDTO.getOrderDate());
        context.setVariable("totalAmount", orderDTO.getTotalAmount().toString());
        context.setVariable("deliveryAddress", customer.getAddress());
        context.setVariable("currentYear", java.time.Year.now());
        // build the order items HTML using StringBuilder
        StringBuilder orderItemsHtml  = new StringBuilder();
        for (OrderItemDTO item: orderDTO.getOrderItems()){
            orderItemsHtml.append("<div class=\"order-item\">")
                    .append("<p>").append(item.getMenu().getName()).append(" x").append(item.getQuantity()).append("</p>")
                    .append("</p>").append("<p> $").append(item.getSubtotal()).append("</p>")
                    .append("</div>");

            context.setVariable("orderItemsHtml", orderItemsHtml.toString());
            context.setVariable("totalItems", orderDTO.getOrderItems().size());
            String paymentLink = basePaymentLink + orderDTO.getId() + "&amount=" + orderDTO.getTotalAmount();// replace mit domain
            context.setVariable("paymentLink", paymentLink);

            // process the thymeleaf template to generate the html email body
            String emailBody = templateEngine.process("order-confirmation", context);// order-confirmation is template name
            notificationService.sendEmail(NotificationDTO.builder()
                    .subject(subject)
                    .body(emailBody)
                    .isHtml(true)
                    .recipient(customer.getEmail())
                    .build());

        }
    }
}
