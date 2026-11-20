package com.xinchao.FoodApp.review.service;

import com.xinchao.FoodApp.auth_users.entity.User;
import com.xinchao.FoodApp.auth_users.services.UserService;
import com.xinchao.FoodApp.enums.OrderStatus;
import com.xinchao.FoodApp.exceptions.BadRequestException;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.menu.entity.Menu;
import com.xinchao.FoodApp.menu.repository.MenuRepository;
import com.xinchao.FoodApp.order.entity.Order;
import com.xinchao.FoodApp.order.repository.OrderItemRepository;
import com.xinchao.FoodApp.order.repository.OrderRepository;
import com.xinchao.FoodApp.response.Response;
import com.xinchao.FoodApp.review.dtos.ReviewDTO;
import com.xinchao.FoodApp.review.entity.Review;
import com.xinchao.FoodApp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService{
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<ReviewDTO> createReview(ReviewDTO reviewDTO) {
        // get current user
        User customer = userService.getCurrentLogInUser();

        // validate required fields
        if(reviewDTO.getOrderId() == null || reviewDTO.getMenuId() == null){
            throw new BadRequestException("Order id and menu item id are required");
        }

        // validate menu item exist
        Menu menu = menuRepository.findById(reviewDTO.getMenuId())
                .orElseThrow(()-> new NotFoundException("Menu item not found"));

        // validate order exist
        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(()-> new NotFoundException("Order not found"));

        // order muss belong to user
        if(!customer.getId().equals(order.getUser().getId())){
            throw new BadRequestException("This order not belong to you");
        }
        // validate order status is DELIVERED
        if(order.getOrderStatus() != OrderStatus.DELIVERED){
            throw new BadRequestException("You can only review items from delivery orders");
        }
        // validate that menu item was part of this order
        boolean itemInOdrder = orderItemRepository.existsByOrderIdAndMenuId(
                reviewDTO.getOrderId(),
                reviewDTO.getMenuId()
        );

        if(!itemInOdrder){
            throw new BadRequestException("This menu item was not part of specificed order!");
        }

        // check if user already wrote a review for the item
        if(reviewRepository.existsByUserIdAndOrderIdAndMenuId(
                customer.getId(),
                reviewDTO.getMenuId(),
                reviewDTO.getOrderId()
        )){
            throw new BadRequestException("You've already reviewed this item from this order.");
        }

        Review review = Review.builder()
                .user(customer)
                .menu(menu)
                .orderId(reviewDTO.getOrderId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        ReviewDTO newReviewDto = modelMapper.map(savedReview, ReviewDTO.class);
        newReviewDto.setUserName(customer.getName());
        newReviewDto.setMenuName(menu.getName());

        return Response.<ReviewDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("review successfully")
                .data(newReviewDto)
                .build();
    }

    @Override
    public Response<List<ReviewDTO>> getReviewsForMenu(Long menuId) {
        List<Review> reviewList = reviewRepository.findByMenuIdOrderByIdDesc(menuId);
        List<ReviewDTO> reviewDTOS = reviewList.stream()
                .map( review -> modelMapper.map(review, ReviewDTO.class))
                .toList();

        return Response.<List<ReviewDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("review successfully")
                .data(reviewDTOS)
                .build();
    }

    @Override
    public Response<Double> getAverageRating(Long menuId) {

        Double avgRating = reviewRepository.calculateAverageRatingByMenuId(menuId);

        return Response.<Double>builder()
                .statusCode(HttpStatus.OK.value())
                .message("review successfully")
                .data(avgRating != null ? avgRating : 0.0 )
                .build();
    }
}
