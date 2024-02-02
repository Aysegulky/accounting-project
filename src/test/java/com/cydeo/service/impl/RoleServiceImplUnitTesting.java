package com.cydeo.service.impl;

import com.cydeo.dto.RoleDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Role;
import com.cydeo.exception.RoleNotFoundException;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplUnitTesting {

    @Mock
    RoleRepository roleRepository;
    @Mock
    MapperUtil mapperUtil;
    @Mock
    SecurityServiceImpl securityServiceImpl;
    @InjectMocks
    RoleServiceImpl roleServiceImpl;

    @Test
    void should_throw_exception_when_roles_not_found() {

        //given
        Long id = 1L;

        //when
        when(roleRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        //then
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class, () ->
            roleServiceImpl.findById(id)
        );
        assertEquals("Role not found with this id:" + id, exception.getMessage());
    }


    @Test
    void should_execute_mapper_at_least_once_and_should_return_what_is_converted() {

        // Given
        // We set up the initial conditions for the test.
        // In this case, we define the input values and the expected output.
        Long id = 1L;// The ID for the role we want to find.
        Role role = new Role();// A new Role instance.
        RoleDTO roleDTO = new RoleDTO();// A new RoleDTO instance.

        // When
        // We specify the behavior that we want to test.
        // Here, we set up the mock objects to return specific values when certain methods are called.
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(mapperUtil.convert(any(Role.class), any(RoleDTO.class))).thenReturn(roleDTO);

        // Action
        // We perform the actual action that we want to test.
        // In this case, we call the method we are testing.
        RoleDTO convertedRoleDTO = roleServiceImpl.findById(id);

        // Then
        // We verify the outcome of the action.
        // Here, we assert that certain conditions are met based on the action we performed.
        verify(mapperUtil, times(1)).convert(any(Role.class), any(RoleDTO.class));// Verify that the convert method was called exactly once with any Role and any RoleDTO

        assertNotNull(convertedRoleDTO);// Assert that the convertedRoleDTO is not null.
        assertSame(roleDTO, convertedRoleDTO);// Assert that the convertedRoleDTO is the same instance as roleDTO.
    }

    @Test
    void should_list_all_Roles() {

        // Given
        // We create two Role objects with IDs and descriptions
        Role role1 = new Role();
        role1.setId(1L);
        role1.setDescription("Role 1");

        Role role2 = new Role();
        role1.setId(2L);
        role1.setDescription("Role 2");

        // We create a list containing the two Role objects
        List<Role> roleList = Arrays.asList(role1, role2);

        // We set up the behavior of the roleRepository mock.
        // When findAll() is called, it will return the roleList we created earlier
        when(roleRepository.findAll()).thenReturn(roleList);

        RoleDTO roleDTO1 = new RoleDTO();
        roleDTO1.setId(1L);
        roleDTO1.setDescription("Role 1");

        RoleDTO roleDTO2 = new RoleDTO();
        roleDTO2.setId(2L);
        roleDTO2.setDescription("Role 2");

        // We set up the behavior of the mapperUtil mock.
        // When convert() is called with any Role and any RoleDTO, it will return
        // roleDTO1 for the first call and roleDTO2 for the second call
        when(mapperUtil.convert(any(Role.class), any(RoleDTO.class))).thenReturn(roleDTO1, roleDTO2);

        // When
        // We call the getAllRoles() method of roleServiceImpl
        List<RoleDTO> result = roleServiceImpl.getAllRoles();

        // Then
        // We verify that the result contains two elements
        assertEquals(2, result.size());
        // We verify that the first element of the result is roleDTO1
        assertEquals(roleDTO1, result.get(0));
        // We verify that the second element of the result is roleDTO2
        assertEquals(roleDTO2, result.get(1));
    }

    @Test
    void should_get_All_Roles_For_Current_User_Except_Root() {
         // Given
        // Create a mock user with a non-root role
        UserDTO user = new UserDTO();
        RoleDTO userRole = new RoleDTO();
        userRole.setId(2L); // Non-root role
        userRole.setDescription("Admin");
        user.setRole(userRole);

        // Mock the behavior of the security service to return the mock user
        when(securityServiceImpl.getLoggedInUser()).thenReturn(user);

        // Mock the behavior of the role repository to return a list of admin roles
        Role adminRole1 = new Role();
        adminRole1.setId(1L);
        adminRole1.setDescription("Role 1");

        Role adminRole2 = new Role();
        adminRole2.setId(2L);
        adminRole2.setDescription("Role 2");

        List<Role> adminRoles = Arrays.asList(adminRole1, adminRole2);
        when(roleRepository.getAllRoleForAdmin("Admin")).thenReturn(adminRoles);

        // Mock the behavior of the mapper utility to convert roles to role DTOs
        RoleDTO roleDTO1 = new RoleDTO();
        roleDTO1.setId(1L);
        roleDTO1.setDescription("Role 1");

        RoleDTO roleDTO2 = new RoleDTO();
        roleDTO2.setId(2L);
        roleDTO2.setDescription("Role 2");

        when(mapperUtil.convert(any(Role.class), any(RoleDTO.class))).thenReturn(roleDTO1, roleDTO2);

        // When
        // Call the method under test
        List<RoleDTO> result = roleServiceImpl.getAllRolesForCurrentUser();

        // Then
        // Verify that the result contains the expected role DTOs
        assertEquals(2, result.size());
        assertEquals(roleDTO1, result.get(0));
        assertEquals(roleDTO2, result.get(1));


    }

    @Test
    void should_return_root_role_for_root_user() {
        // Given
        // Create a mock user with a root role
        UserDTO user = new UserDTO();
        RoleDTO userRole = new RoleDTO();
        userRole.setId(1L); // Root role
        userRole.setDescription("Root");
        user.setRole(userRole);

        // Mock the behavior of the security service to return the mock user
        when(securityServiceImpl.getLoggedInUser()).thenReturn(user);

        // Mock the behavior of the role repository to return the root role
        Role rootRole = new Role();
        rootRole.setId(1L);
        rootRole.setDescription("Root");

        when(roleRepository.getAllRoleForRoot("Root")).thenReturn(rootRole);

        // Mock the behavior of the mapper utility to convert the root role to a role DTO
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setDescription("Root");

        when(mapperUtil.convert(any(Role.class), any(RoleDTO.class))).thenReturn(roleDTO);

        // When
        // Call the method under test
        List<RoleDTO> result = roleServiceImpl.getAllRolesForCurrentUser();

        // Then
        // Verify that the result contains the expected root role DTO
        assertEquals(1, result.size());
        assertEquals(roleDTO, result.get(0));
    }


}
