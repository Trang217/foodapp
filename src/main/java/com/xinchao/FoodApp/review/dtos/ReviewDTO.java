package com.xinchao.FoodApp.review.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {
    private Long id;

    private Long orderId;

    private Long menuId;

    private String userName;

    @NotNull(message = "Rating is required")
    @Min(1)
    @Max(10)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private String menuName;

    private LocalDateTime createdAt;
}
