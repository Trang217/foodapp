package com.xinchao.FoodApp.response;

import com.xinchao.FoodApp.exceptions.BadRequestException;
import com.xinchao.FoodApp.exceptions.NotFoundException;
import com.xinchao.FoodApp.role.dtos.RoleDTO;
import com.xinchao.FoodApp.role.entity.Role;
import com.xinchao.FoodApp.role.repository.RoleRepository;
import com.xinchao.FoodApp.role.services.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    private  final ModelMapper modelMapper;
    @Override
    public Response<RoleDTO> createRole(RoleDTO roleDTO) {
        Role role = modelMapper.map(roleDTO, Role.class);
        Role savedRole = roleRepository.save(role);
        return Response.<RoleDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role created successfully")
                .data(modelMapper.map(savedRole, RoleDTO.class))
                .build();
    }

    @Override
    public Response<RoleDTO> updateRole(RoleDTO roleDTO) {
       Role existingRole = roleRepository.findById(roleDTO.getId())
               .orElseThrow(() -> new NotFoundException("Role not found"));

       if(roleRepository.findByName(roleDTO.getName()).isPresent()){
           throw new BadRequestException("Role with name already exist");
       }

       existingRole.setName(roleDTO.getName());
       Role updatedRole = roleRepository.save(existingRole);

       return Response.<RoleDTO>builder()
               .statusCode(HttpStatus.OK.value())
               .message("Role updated successfully")
               .data(modelMapper.map(updatedRole, RoleDTO.class))
               .build();
    }

    @Override
    public Response<List<RoleDTO>> getAllRoles() {
        List<Role> roleList = roleRepository.findAll();
        List<RoleDTO> roleDTOList = roleList.stream()
                .map(role -> modelMapper.map(role, RoleDTO.class))
                .toList();

        return Response.<List<RoleDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("roles list")
                .data(roleDTOList)
                .build();
    }

    @Override
    public Response<Void> deleteRole(Long roleId) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        roleRepository.delete(existingRole);
        return Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("delete ok")
                .build();
    }
}
