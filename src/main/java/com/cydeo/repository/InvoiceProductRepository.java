package com.cydeo.repository;

import com.cydeo.dto.InvoiceDTO;
import com.cydeo.entity.Company;
import com.cydeo.entity.Invoice;
import com.cydeo.entity.InvoiceProduct;
import com.cydeo.enums.InvoiceStatus;
import com.cydeo.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceProductRepository extends JpaRepository<InvoiceProduct,Long> {
    List<InvoiceProduct> findByInvoiceId(Long invoiceId);

    boolean existsByProductId(Long productId);

    List<InvoiceProduct> findAllByInvoice_InvoiceStatus(InvoiceStatus invoiceStatus);

    List<InvoiceProduct> findByInvoice_CompanyAndInvoice_InvoiceStatusOrderByInsertDateTime(Company company, InvoiceStatus invoiceStatus);
    List<InvoiceProduct> findByInvoice_Company_TitleAndProduct_NameAndInvoice_InvoiceTypeAndRemainingQuantityGreaterThanOrderByInsertDateTimeAsc(
            String companyName, String productName, InvoiceType invoiceType, int quantity);

    @Query(value = "SELECT COALESCE(SUM(ip.profitLoss), 0.00) " +
            "FROM InvoiceProduct ip " +
            "WHERE YEAR(ip.insertDateTime) = :year " +
            "AND MONTH(ip.insertDateTime) = :month " +
            "AND ip.invoice.company.id = :companyId " +
            "AND ip.invoice.invoiceType = :invoiceType ")
    BigDecimal getTotalProfitLossForMonthAndCompanyAndInvoiceType(int year, int month, Long companyId, InvoiceType invoiceType);

    @Query(value = "SELECT COALESCE(SUM(ip.profitLoss), 0.00) " +
            "FROM InvoiceProduct ip " +
            "WHERE ip.product.id = :productId "+
            "AND ip.invoice.company.id = :companyId " +
            "AND ip.invoice.invoiceType = :invoiceType ")
    BigDecimal getProductProfitLoss(Long productId, Long companyId, InvoiceType invoiceType);
}
