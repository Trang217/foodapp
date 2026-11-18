package com.xinchao.FoodApp.cart.service;

import com.xinchao.FoodApp.auth_users.entity.User;
import com.xinchao.FoodApp.auth_users.services.UserService;
import com.xinchao.FoodApp.cart.dtos.CartDTO;
import com.xinchao.FoodApp.cart.entity.Cart;
import com.xinchao.FoodApp.cart.entity.CartItem;
import com.xinchao.FoodApp.cart.repository.CartItemRepository;
import com.xinchao.FoodApp.cart.repository.CartRepository;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.menu.entity.Menu;
import com.xinchao.FoodApp.menu.repository.MenuRepository;
import com.xinchao.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService{
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Response<?> addItemToCart(CartDTO cartDTO) {
        log.info("inside addItemToCart");
        Long menuId = cartDTO.getMenuId();
        int quantity = cartDTO.getQuantity();
        User user = userService.getCurrentLogInUser();
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu not found!"));

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseGet(()-> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
        // Check if the item is already in the cart
        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getMenu().getId().equals(menuId))
                .findFirst();

        // if present, increment it
        if(optionalCartItem.isPresent()){
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity) ;
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);
        } else {
            // if not present, and add it
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(quantity)
                    .pricePerUnit(menu.getPrice())
                    .subTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();
            cart.getCartItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
        }

        cartRepository.save(cart);// no need, it will auto save and persosts in the cart table

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("add cart item successfully")
                .build();
    }

    @Override
    public Response<?> incrementItem(Long menuId) {
        log.info("inside incrementItem");
        User user = userService.getCurrentLogInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("menu not found in cart"));
        int newQuantity = cartItem.getQuantity() + 1;
        cartItem.setQuantity(newQuantity);
        cartItem.setSubTotal(cartItem.getMenu().getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        cartItemRepository.save(cartItem);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("increase cart item successfully")
                .build();
    }

    @Override
    public Response<?> decrementItem(Long menuId) {
        log.info("inside decrementItem");
        User user = userService.getCurrentLogInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        int newQuantity = cartItem.getQuantity() - 1;

        if(newQuantity > 0){
            cartItem.setQuantity(newQuantity);
            cartItem.setSubTotal(cartItem.getMenu().getPrice().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.save(cartItem);
        } else {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("decrease cart item successfully")
                .build();
    }

    @Override
    public Response<?> removeItem(Long cartItemId) {
        log.info("inside removeItem");
        User user = userService.getCurrentLogInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(()-> new NotFoundException("Cart item not found"));


        if(!cart.getCartItems().contains(cartItem)){
            throw  new NotFoundException("cart item does not belong to this user's cart");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("remove cart item successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CartDTO> getShoppingCart() {
        log.info("inside getShoppingCart");
        User user = userService.getCurrentLogInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found"));

        List<CartItem> cartItems = cart.getCartItems();
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        if(cartItems != null){
            for(CartItem item : cartItems ){
                totalAmount = totalAmount.add(item.getSubTotal());
            }
        }
        cartDTO.setTotalAmount(totalAmount);
        // remove the reviews from the response
        if(cartDTO.getCartItems() != null){
           cartDTO.getCartItems()
                   .forEach(item -> item.getMenu().setReviews(null));
        }

        return Response.<CartDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("retrieve cart successfully")
                .data(cartDTO)
                .build();
    }

    @Override
    public Response<?> clearShoppingCart() {
        log.info("inside clearShoppingCart");
        User user = userService.getCurrentLogInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found"));

        // delete all cart item in database first
        cartItemRepository.deleteAll(cart.getCartItems());
        // clear the cart items collection
        cart.getCartItems().clear();
        // save the cart to update the database
        cartRepository.save(cart);

        return Response.<CartDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("cleared cart successfully")
                .build();
    }
}
