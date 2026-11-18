package com.xinchao.FoodApp.menu.controller;

import com.xinchao.FoodApp.menu.dtos.MenuDTO;
import com.xinchao.FoodApp.menu.service.MenuService;
import com.xinchao.FoodApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menu")
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/all")
    public ResponseEntity<Response<List<MenuDTO>>> getAllmenus(@RequestParam(required = false) Long catId,
                                                               @RequestParam(required = false) String search){
        return ResponseEntity.ok(menuService.getAllMenus(catId, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<MenuDTO>> getMenuById(@PathVariable Long id){
        return ResponseEntity.ok(menuService.getMenuById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<MenuDTO>> createMenu(@ModelAttribute @Valid MenuDTO menuDTO,
                                                        @RequestPart(value = "imageFile", required = true) MultipartFile imageFile ){
        menuDTO.setImageFile(imageFile);
        return ResponseEntity.ok(menuService.createMenu(menuDTO));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<MenuDTO>> updateMenu(@ModelAttribute MenuDTO menuDTO,
                                                        @RequestPart(value = "imageFile", required = true) MultipartFile imageFile ){
        menuDTO.setImageFile(imageFile);
        return ResponseEntity.ok(menuService.updateMenu(menuDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<?>> deleteMenuById(@PathVariable Long id){
        return ResponseEntity.ok(menuService.deleteMenuById(id));
    }
}
