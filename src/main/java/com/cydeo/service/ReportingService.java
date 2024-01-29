package com.cydeo.service;

import com.cydeo.dto.InvoiceProductDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ReportingService {
    List<InvoiceProductDTO> getInvoiceProductList();

    List<Map.Entry<String ,BigDecimal>> getMonthlyProfitLossListMap();

    List<Map.Entry<String ,BigDecimal>> getProductProfitLossListMap();
}
