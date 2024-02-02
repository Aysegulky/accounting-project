package com.cydeo.controller;

import com.cydeo.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;

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

    @GetMapping("/profitLossData/table")
    public String getProfitLossList(Model model){

        model.addAttribute("monthlyProfitLossDataMap",reportingService.getMonthlyProfitLossListMap());

        return "report/profit-loss-report";
    }
    @GetMapping("/productProfitLoss/table")
    public String getProductProfitLossTableData(Model model){

        model.addAttribute("productProfitLossDataMap",reportingService.getProductProfitLossListMap());

        return "report/product-profit-loss-table";
    }
    @GetMapping("/productProfitLoss/bar")
    public String getProductProfitLossBarData(Model model, @RequestParam( required = false,defaultValue = "1") int p){

        List<Map.Entry<String, BigDecimal>> barChartData = reportingService.getProductProfitLossListMap();
        List<String> pageOptions = reportingService.generatePageOptions(barChartData.size());
        int scaleNum = reportingService.getScaleNum();

        barChartData = reportingService.getSublistByPage(barChartData,p,10);

        model.addAttribute("barChartData",barChartData);
        model.addAttribute("scaleNum",scaleNum);
        model.addAttribute("pageViewOptions",pageOptions);

        return "report/product-profit-loss-barchart";
    }
    @GetMapping("/productProfitLoss/pie")
    public String getProductProfitLossPieData(Model model, @RequestParam( required = false,defaultValue = "1") int p){

        List<Map.Entry<String, BigDecimal>> pieChartData = reportingService.getProductProfitLossListMap();
        List<String> pageOptions = reportingService.generatePageOptions(pieChartData.size());

        pieChartData = reportingService.getSublistByPage(pieChartData,p,10);

        model.addAttribute("barChartData",pieChartData);
        model.addAttribute("pageViewOptions",pageOptions);

        return "/report/product-profit-loss-pieChart";
    }
}
