package com.cydeo.service.impl;

import com.cydeo.dto.InvoiceDTO;
import com.cydeo.dto.InvoiceProductDTO;
import com.cydeo.entity.Invoice;
import com.cydeo.enums.InvoiceStatus;
import com.cydeo.enums.InvoiceType;
import com.cydeo.exception.InvoiceNotFoundException;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class InvoiceServiceImplUnitTest {


    @Mock
    InvoiceRepository invoiceRepository;
    @Mock
    MapperUtil mapper;
    @Mock
    InvoiceProductServiceImpl invoiceProductServiceImpl;
    @InjectMocks
    InvoiceServiceImpl invoiceServiceImpl;


    @ExtendWith(MockitoExtension.class)
    @Test
    public void should_throw_exception_when_invoice_not_found() {

        Mockito.when(invoiceRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        Throwable throwable = catchThrowable(() -> invoiceServiceImpl.findById(1L));
        assertThat(throwable).isInstanceOf(InvoiceNotFoundException.class);
        assertThat(throwable).hasMessage("Invoice can not found with id: " + 1L);

    }

    @Test
    void should_calculate_tax_for_given_Product() {

        InvoiceProductDTO invoiceProductDTO = new InvoiceProductDTO();
        invoiceProductDTO.setPrice(BigDecimal.valueOf(100));
        invoiceProductDTO.setTax(10);
        invoiceProductDTO.setQuantity(5);

        BigDecimal taxAmount = invoiceServiceImpl.calculateTaxForProduct(invoiceProductDTO);

        assertEquals(BigDecimal.valueOf(50), taxAmount);

    }

    @Test
    void should_throw_exception_when_calculating_negative_tax_for_given_Product() {
        // Given
        InvoiceProductDTO invoiceProductDTO = new InvoiceProductDTO();
        invoiceProductDTO.setPrice(BigDecimal.valueOf(100));
        invoiceProductDTO.setTax(-10); // negative tax percentage
        invoiceProductDTO.setQuantity(2);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            invoiceServiceImpl.calculateTaxForProduct(invoiceProductDTO);
        });

        // Then
        assertEquals("Tax amount cannot be negative!", exception.getMessage());
    }

    @Test
    void calculate_total_price_without_Tax() {
        // Given
        InvoiceProductDTO product1 = new InvoiceProductDTO();
        product1.setPrice(BigDecimal.valueOf(100)); // Set the price of the first product to 100
        product1.setQuantity(2); // Set the quantity of the first product to 2

        InvoiceProductDTO product2 = new InvoiceProductDTO();
        product2.setPrice(BigDecimal.valueOf(50)); // Set the price of the second product to 50
        product2.setQuantity(3); // Set the quantity of the second product to 3

        List<InvoiceProductDTO> invoiceProductDTOList = Arrays.asList(product1, product2); // Create a list of InvoiceProductDTOs

        // When
        BigDecimal totalPriceWithoutTax = invoiceServiceImpl.calculateTotalPriceWithoutTax(invoiceProductDTOList); // Calculate the total price without tax

        // Then
        assertEquals(BigDecimal.valueOf(350), totalPriceWithoutTax); // Verify that the calculated total price without tax is 350 (100 * 2 + 50 * 3 = 350)
    }


    @Test
    void delete() { // TODO it doesnt work
        // Given
        Long invoiceId = 1L;
        Invoice invoiceToDelete = new Invoice();
        invoiceToDelete.setId(invoiceId);

        // Mock the behavior of the repository
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoiceToDelete));

        // Mock the behavior of the mapper
        when(mapper.convert(invoiceToDelete, new InvoiceDTO())).thenReturn(new InvoiceDTO());

        // When
        invoiceServiceImpl.deleteInvoice(invoiceId);

        // Then
        verify(invoiceRepository, times(1)).findById(invoiceId);
        verify(invoiceRepository, times(1)).save(invoiceToDelete);
        verify(invoiceProductServiceImpl, times(1)).deleteByInvoice(any(InvoiceDTO.class));
    }

    @Test
    void should_approve_invoice() { //TODO I couldnt solve
        // Given
        Long invoiceId = 1L;

        Invoice invoiceToApprove = new Invoice();
        invoiceToApprove.setId(invoiceId);
        invoiceToApprove.setInvoiceStatus(InvoiceStatus.APPROVED);
        invoiceToApprove.setInvoiceType(InvoiceType.SALES);


    }


}
