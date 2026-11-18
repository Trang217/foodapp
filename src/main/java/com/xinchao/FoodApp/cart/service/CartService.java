package com.xinchao.FoodApp.cart.service;

import com.xinchao.FoodApp.cart.dtos.CartDTO;
import com.xinchao.FoodApp.response.Response;

public interface CartService {
     Response<?> addItemToCart(CartDTO cartDTO);
     Response<?> increatementItem(Long menuId);
     Response<?> decreatementItem(Long menuId);
     Response<?> removeItem(Long cartItemId);
     Response<CartDTO> getShoppingCart();
     Response<?> clearShoppingCart();

}
