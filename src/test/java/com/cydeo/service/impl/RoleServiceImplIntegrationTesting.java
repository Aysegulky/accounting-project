package com.cydeo.service.impl;

import com.cydeo.dto.RoleDTO;
import com.cydeo.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class RoleServiceImplIntegrationTesting {

    @Autowired
    private RoleServiceImpl roleService;

    @Test
    void should_find_role_by_id() {
        // Given
        Long roleId = 2L;
        Role role = new Role();
        role.setId(roleId);
        role.setDescription("Admin");

        RoleDTO expectedRoleDTO = new RoleDTO();
        expectedRoleDTO.setId(roleId);
        expectedRoleDTO.setDescription("Admin");


        // When
        RoleDTO resultRoleDTO = roleService.findById(roleId);

        // Then
        assertThat(resultRoleDTO).isNotNull();
        assertThat(resultRoleDTO.getId()).isEqualTo(roleId);
        assertThat(resultRoleDTO.getDescription()).isEqualTo("Admin");
    }





}
