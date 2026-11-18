package com.xinchao.FoodApp.menu.service;

import com.xinchao.FoodApp.aws.AWSS3Service;
import com.xinchao.FoodApp.category.entity.Category;
import com.xinchao.FoodApp.category.repository.CategoryRepository;
import com.xinchao.FoodApp.exceptions.BadRequestException;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.menu.dtos.MenuDTO;
import com.xinchao.FoodApp.menu.entity.Menu;
import com.xinchao.FoodApp.menu.repository.MenuRepository;
import com.xinchao.FoodApp.response.Response;
import com.xinchao.FoodApp.review.dtos.ReviewDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService{
    private final MenuRepository menuRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    private final AWSS3Service awss3Service;

    @Override
    public Response<MenuDTO> createMenu(MenuDTO menuDTO) {
        log.info("inside createMenu()");
        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        String imageUrl = null;
        MultipartFile imageFile = menuDTO.getImageFile();

        if(imageFile == null || imageFile.isEmpty()){
            throw new BadRequestException("Menu image is required");
        }

        String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        URL s3Url = awss3Service.loadFile("menus/" + imageName, imageFile);
        imageUrl = s3Url.toString();

        Menu menu = Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .price(menuDTO.getPrice())
                .imageUrl(imageUrl)
                .category(category)
                .build();
        menuRepository.save(menu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Add menu successfully")
                .data(modelMapper.map(menu, MenuDTO.class))
                .build();
    }

    @Override
    public Response<MenuDTO> updateMenu(MenuDTO menuDTO) {
        log.info("inside updateMenu()");
        Menu menu = menuRepository.findById(menuDTO.getId())
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        String imageUrl = menu.getImageUrl();
        MultipartFile imageFile = menuDTO.getImageFile();
        if(imageFile != null && !imageFile.isEmpty()){
            // check if the new file was provided
            if(imageUrl != null && !imageUrl.isEmpty()){
                // delete the old file in s3 if it exists
                String keyName = imageUrl.substring(imageUrl.lastIndexOf("/" ) + 1);
                awss3Service.deleteFile("menus/" + keyName);
                log.info("delete menu file from s3");
            }
            // upload new image
            String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            URL newMenuImgUrl = awss3Service.loadFile("menus/" + imageName, imageFile);
            imageUrl = newMenuImgUrl.toString();
        }

        if(menuDTO.getName() != null && !menuDTO.getName().isBlank()){
            menu.setName(menuDTO.getName());
        }

        if(menuDTO.getDescription() != null && !menuDTO.getDescription().isBlank()){
            menu.setDescription(menuDTO.getDescription());
        }

        if(menuDTO.getPrice() != null){
            menu.setPrice(menuDTO.getPrice());
        }

        menu.setImageUrl(imageUrl);
        menu.setCategory(category);

        Menu updatedMenu = menuRepository.save(menu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("update menu successfully")
                .data(modelMapper.map(updatedMenu, MenuDTO.class))
                .build();
    }

    @Override
    public Response<MenuDTO> getMenuById(Long menuId) {
        log.info("inside getMenuById");
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        MenuDTO menuDTO = modelMapper.map(menu, MenuDTO.class);

        // sort reviews desc
        if(menuDTO.getReviews() != null){
            menuDTO.getReviews().sort(Comparator.comparing(ReviewDTO::getId).reversed());
        }

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("get menu successfully")
                .data(menuDTO)
                .build();
    }

    @Override
    public Response<?> deleteMenuById(Long menuId) {
        log.info("inside deleteMenuById");
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(()-> new NotFoundException("Menu not found"));
        // delete image from s3 if it exists
        String imageUrl = menu.getImageUrl();
        if(imageUrl != null && !imageUrl.isEmpty()){
            String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            awss3Service.deleteFile("menus/" + keyName);
        }
        menuRepository.deleteById(menuId);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("deleted menu successfully")
                .build();
    }

    @Override
    public Response<List<MenuDTO>> getAllMenus(Long categoryId, String search) {
        log.info("inside getAllMenus");

        Specification<Menu> spec = buildSpecification(categoryId, search);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        List<Menu> menuList = menuRepository.findAll(spec, sort);

        List<MenuDTO> menuDTOList = menuList.stream()
                .map(menu -> modelMapper.map(menu, MenuDTO.class))
                .toList();

        return Response.<List<MenuDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("get all menus successfully")
                .data(menuDTOList)
                .build();
    }

    private Specification<Menu> buildSpecification(Long categoryId, String search){
        return ((root, query, cb) ->
        {
            // List to accumulate all where conditions
            List<Predicate> predicates = new ArrayList<>();

            // add category filter if category id is provided
            if (categoryId != null) {
                predicates.add(cb.equal(
                        root.get("category").get("id"),
                        categoryId
                ));
            }

            if(search != null && !search.isBlank()){
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(
                                cb.lower(root.get("name")),
                                searchTerm
                        ),
                        cb.like(
                                cb.lower(root.get("description")),
                                searchTerm
                                ))
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        });
    }
}
