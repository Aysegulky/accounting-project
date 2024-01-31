package com.cydeo.controller;

import com.cydeo.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportingController {
    private final ReportingService reportingService;

    @GetMapping("/stockData")
    public String getStockReportList(Model model){

        model.addAttribute("invoiceProducts",reportingService.getInvoiceProductList());

        return "report/stock-report";
    }

    @GetMapping("/profitLossData")
    public String getProfitLossList(Model model){

        model.addAttribute("monthlyProfitLossDataMap",reportingService.getMonthlyProfitLossListMap());

        return "report/profit-loss-report";
    }
    @GetMapping("/productProfitLoss")
    public String getProfitLossListForProduct(Model model){

        model.addAttribute("productProfitLossDataMap",reportingService.getProductProfitLossListMap());

        return "report/product-profit-loss-barchart";
    }
}
