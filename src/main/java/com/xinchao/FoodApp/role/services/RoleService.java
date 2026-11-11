package com.xinchao.FoodApp.role.services;

import com.xinchao.FoodApp.response.Response;
import com.xinchao.FoodApp.role.dtos.RoleDTO;

import java.util.List;

public interface RoleService {
    Response<RoleDTO> createRole(RoleDTO roleDTO);

    Response<RoleDTO> updateRole(RoleDTO roleDTO);

    Response<List<RoleDTO>> getAllRoles();

    Response<Void> deleteRole(Long roleId);
}
