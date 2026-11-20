package com.xinchao.FoodApp.review.controller;

import com.xinchao.FoodApp.response.Response;
import com.xinchao.FoodApp.review.dtos.ReviewDTO;
import com.xinchao.FoodApp.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Response<ReviewDTO>> createReview(@RequestBody @Valid ReviewDTO reviewDTO){
        return ResponseEntity.ok(reviewService.createReview(reviewDTO));
    }

    @GetMapping("/{menuId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<ReviewDTO>>> getReviewsForMenu(@PathVariable Long menuId){
        return ResponseEntity.ok(reviewService.getReviewsForMenu(menuId));
    }

    @GetMapping("/avg-rating/{menuId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<Double>> getAverageRating(@PathVariable Long menuId){
        return ResponseEntity.ok(reviewService.getAverageRating(menuId));
    }
}
