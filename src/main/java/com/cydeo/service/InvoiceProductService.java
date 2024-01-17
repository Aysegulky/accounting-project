package com.cydeo.service;

import com.cydeo.dto.InvoiceDTO;
import com.cydeo.dto.InvoiceProductDTO;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface InvoiceProductService {
    InvoiceProductDTO findById(Long id);
    List<InvoiceProductDTO> findByInvoiceId(Long invoiceId);
    InvoiceDTO deleteById(Long id);
    void removeInvoiceProductFromInvoice(Long invoiceId, Long invoiceProductId);

    InvoiceProductDTO create(InvoiceProductDTO invoiceProductDTO, Long invoiceId);

    BindingResult doesProductHaveEnoughStock(InvoiceProductDTO invoiceProductDTO, BindingResult bindingResult );
}
