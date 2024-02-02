package com.cydeo.service.impl;


import com.cydeo.client.CountryClient;
import com.cydeo.dto.ClientVendorDTO;
import com.cydeo.entity.ClientVendor;
import com.cydeo.exception.ClientVendorNotFoundException;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.ClientVendorRepository;
import com.cydeo.service.ClientVendorService;
import com.cydeo.service.InvoiceService;
import com.cydeo.service.SecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class ClientVendorServiceImplIntegrationTest {

    @Autowired
    private ClientVendorRepository clientVendorRepository;

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ClientVendorService clientVendorService;

    @Autowired
    private CountryClient countryClient;


    @Test
    void should_find_client_vendor_by_id() {
        // Given
        Long existingClientId = 1L;

        // When
        ClientVendorDTO foundClientVendor = clientVendorService.findById(existingClientId);

        // Then
        assertThat(foundClientVendor).isNotNull();

    }

    @Test
    void should_throw_exception_when_client_vendor_not_found_by_id() {
        // Given
        Long nonExistingClientId = 100L;

        // When
        assertThrows(ClientVendorNotFoundException.class, () -> clientVendorService.findById(nonExistingClientId));
    }


  @Test
  public void should_update_client_vendor_successfully() {
      // GIVEN
      Long clientId = 1L;
      ClientVendorDTO clientVendorDTO = new ClientVendorDTO();
      clientVendorDTO.setClientVendorName("Updated Client Vendor");

      // WHEN
      ClientVendorDTO updatedClientVendor = clientVendorService.update(clientId, clientVendorDTO);

      // THEN
      assertThat(updatedClientVendor).isNotNull();
      // Add assertions based on your specific scenario
      assertEquals("Updated Client Vendor", updatedClientVendor.getClientVendorName());
  }


    @Test
    public void should_delete_client_vendor_successfully() {
        // GIVEN
        Long clientId = 1L;

        // WHEN
        clientVendorService.delete(clientId);

        // THEN
        ClientVendor deletedClientVendor = clientVendorRepository.findById(clientId).orElse(null);
        assertFalse(deletedClientVendor != null && deletedClientVendor.getIsDeleted());
    }

    @Test
    public void should_check_if_client_has_invoice_successfully() {
        // GIVEN
        Long clientId = 1L;

        // WHEN
        boolean hasInvoice = clientVendorService.isClientHasInvoice(clientId);

        // THEN
        assertTrue(hasInvoice);
    }

    @Test
    public void should_get_countries_successfully() {
        // WHEN
        List<String> countries = clientVendorService.getCountries();

        // THEN
        assertThat(countries).isNotEqualTo(0);
    }

}
