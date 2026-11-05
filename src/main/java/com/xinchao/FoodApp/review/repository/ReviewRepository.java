package com.xinchao.FoodApp.review.repository;

import com.xinchao.FoodApp.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMenuIdOrderByIdDesc(Long menuId);

    @Query("SELECT AVG(r.rating) FROM Review WHERE r.menu.id = :menuId")
    Double calculateAverageRatingByMenuId(@Param("menuId") Long menuId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Review r " +
            "WHERE r.user.id = :userId AND r.order.id = :orderId AND r.menu.id = :menuId")
    boolean existsByUserIdAndOrderIdAndMenuId(
            @Param("userId") long userId,
            @Param("orderId") long orderId,
            @Param("menuId") long menuId
    );
}
