package com.cydeo.service;

import com.cydeo.dto.InvoiceProductDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ReportingService {
    List<InvoiceProductDTO> getInvoiceProductList();

    List<Map.Entry<String ,BigDecimal>> getMonthlyProfitLossListMap();
    List<Map.Entry<String ,BigDecimal>> getMonthlyProfitLossByYear(Integer year);


    List<Map.Entry<String ,BigDecimal>> getProductProfitLossListMap();

    List<String> generatePageOptions(int dataSize);

    <T> List<T> getSublistByPage(List<T> originalList, int pageNumber, int pageSize);
    int getScaleNum();
    int getScaleNum(List<Map.Entry<String, BigDecimal>> data);

    List<String> generatePageOptionsForProfitLoss();
}
