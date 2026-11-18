package com.xinchao.FoodApp.menu.service;

import com.xinchao.FoodApp.menu.dtos.MenuDTO;
import com.xinchao.FoodApp.response.Response;

import java.util.List;

public interface MenuService {
    Response<MenuDTO> createMenu(MenuDTO menuDTO);
    Response<MenuDTO> updateMenu(MenuDTO menuDTO);
    Response<List<MenuDTO>> getAllMenus(Long categoryId, String search);
    Response<MenuDTO> getMenuById(Long menuId);
    Response<?> deleteMenuById(Long menuId);
}
