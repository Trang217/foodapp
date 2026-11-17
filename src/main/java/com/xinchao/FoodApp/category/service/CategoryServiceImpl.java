package com.xinchao.FoodApp.category.service;

import com.xinchao.FoodApp.category.dtos.CategoryDTO;
import com.xinchao.FoodApp.category.entity.Category;
import com.xinchao.FoodApp.category.repository.CategoryRepository;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<CategoryDTO> addCategory(CategoryDTO categoryDTO) {
        log.info("inside addCategory");
        Category category = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.save(category);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("add category successfully")
                .build();

    }

    @Override
    public Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO) {
        log.info("inside updateCategory");
        Category existingCategory = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new NotFoundException("category not found"));

        if(categoryDTO.getName() != null && !categoryDTO.getName().isEmpty()){
            existingCategory.setName(categoryDTO.getName());
        }

        if(categoryDTO.getDescription() != null && !categoryDTO.getDescription().isEmpty()){
            existingCategory.setDescription(categoryDTO.getDescription());
        }

        categoryRepository.save(existingCategory);

        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("update category successfully")
                .build();
    }

    @Override
    public Response<CategoryDTO> getCategoryById(Long catId) {
        log.info("inside addCategory");
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("category not found"));
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("retrieve category successfully")
                .data(categoryDTO)
                .build();
    }

    @Override
    public Response<List<CategoryDTO>> getAllCategories() {
        log.info("inside getCategoryById");
        List<Category> categoryList = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOList = categoryList.stream()
                .map(cat -> modelMapper.map(cat, CategoryDTO.class))
                .toList();
        return Response.<List<CategoryDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("retrieve category list successfully")
                .data(categoryDTOList)
                .build();
    }

    @Override
    public Response<?> deleteCategory(Long catId) {
        log.info("inside deleteCategory");
        if(!categoryRepository.existsById(catId)){
           throw  new NotFoundException("category not found");
        }
        categoryRepository.deleteById(catId);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("delete category successfully")
                .build();
    }
}
