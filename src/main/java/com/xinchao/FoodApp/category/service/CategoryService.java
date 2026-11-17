package com.xinchao.FoodApp.category.service;

import com.xinchao.FoodApp.category.dtos.CategoryDTO;
import com.xinchao.FoodApp.response.Response;

import java.util.List;

public interface CategoryService {
    Response<CategoryDTO> addCategory(CategoryDTO categoryDTO);
    Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO);
    Response<CategoryDTO> getCategoryById(Long catId);
    Response<List<CategoryDTO>> getAllCategories();
    Response<?> deleteCategory(Long catId);

}
