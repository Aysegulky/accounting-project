package com.cydeo.service.impl;


import com.cydeo.dto.ClientVendorDTO;
import com.cydeo.dto.CompanyDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.ClientVendor;
import com.cydeo.entity.Company;
import com.cydeo.enums.ClientVendorType;
import com.cydeo.exception.ClientVendorNotFoundException;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.ClientVendorRepository;
import com.cydeo.service.InvoiceService;
import com.cydeo.service.SecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientVendorServiceImplUnitTest {

    @Mock
    private ClientVendorRepository clientVendorRepository;

    @Mock
    private MapperUtil mapperUtil;

    @Mock
    private SecurityService securityService;

    @Mock
    private InvoiceService invoiceService;


    @InjectMocks
    private ClientVendorServiceImpl clientVendorService;

    //findById
    @Test
    void should_throw_exception_when_client_vendor_not_found_with_id() {
        //given
        Long id = 1L;

        //when
        when(clientVendorRepository.findById(id)).thenReturn(Optional.empty());
        Throwable throwable = catchThrowable(() -> clientVendorService.findById(id));

        //then
        assertThat(throwable).isInstanceOf(ClientVendorNotFoundException.class);
    }

    @Test
    void should_execute_mapper_at_least_once_and_should_return_what_is_converted() {
        //given
        Long id = 1L;
        ClientVendor clientVendor = new ClientVendor();
        ClientVendorDTO clientVendorDTO = new ClientVendorDTO();

        when(clientVendorRepository.findById(id)).thenReturn(Optional.of(clientVendor));
        when(mapperUtil.convert(clientVendor, new ClientVendorDTO())).thenReturn(clientVendorDTO);

        //when
        ClientVendorDTO returnedClientVendor = clientVendorService.findById(id);

        //then
        verify(mapperUtil, times(1)).convert(clientVendor, clientVendorDTO);
        assertNotNull(returnedClientVendor);
        assertSame(clientVendorDTO, returnedClientVendor);
    }



    //getAllClientVendors()
    @Test
    void should_return_empty_list_when_no_client_vendors_found() {
        // given
        UserDTO loggedInUser = new UserDTO();
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setId(1L); // Assuming a non-null id value
        loggedInUser.setCompany(companyDTO);

        when(securityService.getLoggedInUser()).thenReturn(loggedInUser);//mock the user for the logged-in user
        when(clientVendorRepository.findAllByCompanyId(anyLong())).thenReturn(Collections.emptyList());

        // when
        List<ClientVendorDTO> clientVendors = clientVendorService.getAllClientVendors();

        // then
        assertTrue(clientVendors.isEmpty());
    }



    //saveClientVendor
    @Test
    public void should_Save_ClientVendor() {
        // given
        UserDTO loggedInUser = new UserDTO();
        CompanyDTO companyDTO = new CompanyDTO();
        loggedInUser.setCompany(companyDTO);

        ClientVendorDTO clientVendorDTO = new ClientVendorDTO();

        when(securityService.getLoggedInUser()).thenReturn(loggedInUser);

        Company company = new Company();
        when(mapperUtil.convert(eq(companyDTO), any())).thenReturn(company);

        ClientVendor clientVendorToSave = new ClientVendor();
        when(mapperUtil.convert(eq(clientVendorDTO), any())).thenReturn(clientVendorToSave);

        ClientVendor savedClientVendor = new ClientVendor();
        when(clientVendorRepository.save(any())).thenReturn(savedClientVendor);

        // when
        ClientVendorDTO result = clientVendorService.saveClientVendor(clientVendorDTO);

        // then
        verify(securityService, times(1)).getLoggedInUser();
        verify(mapperUtil, times(1)).convert(eq(companyDTO), any());
        verify(mapperUtil, times(1)).convert(eq(clientVendorDTO), any());
        verify(clientVendorRepository, times(1)).save(any());
        verify(mapperUtil, times(1)).convert(eq(savedClientVendor), any());

    }


    //findClientVendorByClientVendorTypeAndCompany()
    @Test
    void should_return_empty_list_when_no_matching_client_vendors_found() {
        // given
        UserDTO loggedInUser = new UserDTO();
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setId(1L); // Assuming a non-null id value
        loggedInUser.setCompany(companyDTO);

        when(securityService.getLoggedInUser()).thenReturn(loggedInUser);
        when(clientVendorRepository.findClientVendorByClientVendorTypeAndCompany(any(), any()))
                .thenReturn(Collections.emptyList());

        // when
        List<ClientVendorDTO> clientVendors = clientVendorService.findClientVendorByClientVendorTypeAndCompany(ClientVendorType.VENDOR);

        // then
        assertTrue(clientVendors.isEmpty());
    }



    //delete
    @Test
    void should_delete_client_vendor_and_set_is_deleted_to_true() {
        // given
        Long id = 1L;

        ClientVendor clientVendorToDelete = new ClientVendor();
        clientVendorToDelete.setId(id);
        clientVendorToDelete.setIsDeleted(Boolean.FALSE);

        Optional<ClientVendor> optionalClientVendor = Optional.of(clientVendorToDelete);

        when(clientVendorRepository.findById(id)).thenReturn(optionalClientVendor);
        when(invoiceService.existsByClientVendorId(id)).thenReturn(false);

        // when
        clientVendorService.delete(id);

        // then
        verify(clientVendorRepository).findById(id);
        verify(invoiceService).existsByClientVendorId(id);
        verify(clientVendorRepository).save(clientVendorToDelete);

        assertTrue(clientVendorToDelete.getIsDeleted());
    }

    @Test
    void should_not_delete_client_vendor_with_existing_invoices() {
        // given
        Long id = 1L;

        ClientVendor clientVendorToDelete = new ClientVendor();
        clientVendorToDelete.setId(id);
        clientVendorToDelete.setIsDeleted(Boolean.FALSE);

        Optional<ClientVendor> optionalClientVendor = Optional.of(clientVendorToDelete);

        when(clientVendorRepository.findById(id)).thenReturn(optionalClientVendor);
        when(invoiceService.existsByClientVendorId(id)).thenReturn(true);

        // when
        clientVendorService.delete(id);

        // then
        verify(clientVendorRepository).findById(id);
        verify(invoiceService).existsByClientVendorId(id);
        verify(clientVendorRepository, never()).save(clientVendorToDelete);

        assertFalse(clientVendorToDelete.getIsDeleted());
    }



    //isClientHasInvoice
    @Test
    public void should_Client_Has_Invoice() {

        //given
        Long clientId = 1L;

        // when
        when(invoiceService.existsByClientVendorId(clientId)).thenReturn(true);
        boolean result = clientVendorService.isClientHasInvoice(clientId);

        //then
        assertTrue(result);
        verify(invoiceService).existsByClientVendorId(clientId);
    }


}


