package com.cydeo.service.impl;

import com.cydeo.dto.*;
import com.cydeo.entity.Company;
import com.cydeo.entity.Invoice;
import com.cydeo.enums.InvoiceStatus;
import com.cydeo.enums.InvoiceType;
import com.cydeo.exception.InvoiceNotFoundException;
import com.cydeo.exception.NoEnoughStockException;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.InvoiceRepository;
import com.cydeo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final MapperUtil mapper;
    private final SecurityService securityService;
    private final InvoiceProductService invoiceProductService;
    private final ProductService productService;

    /**
     * Finds invoices by invoiceId based on logged-in user's company. Calculates price, tax amount and total price of the invoice.
     *
     * @param id
     * @return invoiceDTO
     */
    @Override
    public InvoiceDTO findById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice can not found with id: " + id));
        InvoiceDTO invoiceDTO = mapper.convert(invoice, new InvoiceDTO());

        List<InvoiceProductDTO> invoiceProductDTOList = invoiceProductService.findByInvoiceId(invoiceDTO.getId());

        BigDecimal withoutTax = calculateTotalPriceWithoutTax(invoiceProductDTOList);
        BigDecimal tax = calculateTax(invoiceProductDTOList);

        invoiceDTO.setPrice(withoutTax);
        invoiceDTO.setTax(tax);
        invoiceDTO.setTotal(withoutTax.add(tax));

        return invoiceDTO;
    }

    /**
     * Finds all invoices belongs to logged-in user's company. Calculates price, tax amount and total price of all invoices.
     *
     * @param invoiceType
     * @return
     */
    @Override
    public List<InvoiceDTO> findAllInvoices(InvoiceType invoiceType) {
        UserDTO loggedInUser = securityService.getLoggedInUser();

        List<Invoice> all = invoiceRepository.findInvoiceByInvoiceTypeAndCompany_TitleAndIsDeletedOrderByInvoiceNoDesc(invoiceType, loggedInUser.getCompany().getTitle(), false);

        List<InvoiceDTO> invoiceDTOList = all.stream()
                .map(invoice -> mapper.convert(invoice, new InvoiceDTO()))
                .collect(Collectors.toList());

        invoiceDTOList = invoiceDTOList.stream()
                .map(invoice -> {
                    List<InvoiceProductDTO> invoiceProductDTOList = invoiceProductService.findByInvoiceId(invoice.getId());
                    BigDecimal totalPriceWithoutTax = calculateTotalPriceWithoutTax(invoiceProductDTOList);
                    BigDecimal taxAmount = calculateTax(invoiceProductDTOList);

                    invoice.setPrice(totalPriceWithoutTax);
                    invoice.setTax(taxAmount);
                    invoice.setTotal(totalPriceWithoutTax.add(taxAmount));

                    return invoice;
                })
                .collect(Collectors.toList());

        return invoiceDTOList;
    }

    /**
     * Percentage divisor for calculation methods.
     */
    private static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);

    /**
     * Calculates total tax amount of the given invoiceProductDTO objects.
     *
     * @param invoiceProductDTOList
     * @return tax amount (BigDecimal)
     */
    private BigDecimal calculateTax(List<InvoiceProductDTO> invoiceProductDTOList) {
        BigDecimal sum = invoiceProductDTOList.stream()
                .map(this::calculateTaxForProduct)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax cannot be negative!");
        }

        return sum;
    }

    /**
     * Calculates tax amount of the given invoiceProductDTO.
     *
     * @param invoiceProductDTO
     * @return tax amount (BigDecimal)
     */
    @Override
    public BigDecimal calculateTaxForProduct(InvoiceProductDTO invoiceProductDTO) {
        BigDecimal price = invoiceProductDTO.getPrice();
        BigDecimal taxPercentage = BigDecimal.valueOf(invoiceProductDTO.getTax());
        Integer quantity = invoiceProductDTO.getQuantity();

        BigDecimal taxAmount = price.multiply(taxPercentage).divide(PERCENTAGE_DIVISOR, RoundingMode.CEILING).multiply(BigDecimal.valueOf(quantity));

        if (taxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax amount cannot be negative!");
        }

        return taxAmount;
    }

    /**
     * Calculates total amount (without tax) of the given invoiceProductDTO objects.
     *
     * @param invoiceProductDTOList
     * @return Total Price Without Tax (BigDecimal)
     */
    private BigDecimal calculateTotalPriceWithoutTax(List<InvoiceProductDTO> invoiceProductDTOList) {
        BigDecimal sum = invoiceProductDTOList.stream()
                .map(invoiceProductDTO ->
                        (invoiceProductDTO.getPrice()).multiply(BigDecimal.valueOf(invoiceProductDTO.getQuantity())))//total price amount without tax
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum;

    }

    /**
     * Updates the invoice. Id, InvoiceStatus, InvoiceType and Company fields are not updated.
     *
     * @param foundInvoice
     * @param invoiceToUpdate
     */
    // invoiceNo, invoiceStatus, invoiceType, date, company cannot be updatable. Update -> ClientVendor
    // id, invoiceStatus, invoiceType, company details should come from DB
    @Override
    public void update(InvoiceDTO foundInvoice, InvoiceDTO invoiceToUpdate) {
        invoiceToUpdate.setId(foundInvoice.getId());
        invoiceToUpdate.setInvoiceStatus(foundInvoice.getInvoiceStatus());
        invoiceToUpdate.setInvoiceType(foundInvoice.getInvoiceType());
        invoiceToUpdate.setCompany(foundInvoice.getCompany());

        Invoice converted = mapper.convert(invoiceToUpdate, new Invoice());
        invoiceRepository.save(converted);
    }

    /**
     * Softly deletes the invoice.
     *
     * @param invoiceId
     */
    @Override
    public void deleteInvoice(Long invoiceId) {
        Invoice invoiceToDelete = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice can not found with id: " + invoiceId));

        //delete operation
        invoiceToDelete.setIsDeleted(Boolean.TRUE);

        // delete the invoiceProduct that related with this invoice
        invoiceProductService.deleteByInvoice(mapper.convert(invoiceToDelete, new InvoiceDTO()));

        invoiceRepository.save(invoiceToDelete);
    }

    /**
     * Approves the invoice
     *
     * @param invoiceId
     */
    @Override
    public void approve(Long invoiceId) {
        Invoice invoiceToApprove = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice can not found with id: " + invoiceId));

        List<InvoiceProductDTO> invoiceProductList = invoiceProductService.findByInvoiceId(invoiceToApprove.getId());

        if (invoiceToApprove.getInvoiceType() == InvoiceType.SALES) {
            if ((validateInvoiceProducts(invoiceProductList, invoiceId))) {
                saveSalesInvoiceProductProfitLossAndRemainingQuantity(invoiceProductList);
                decreaseProductRemainingQuantity(invoiceProductList);
            }
        } else if (invoiceToApprove.getInvoiceType() == InvoiceType.PURCHASE) {
            savePurchaseInvoiceProductProfitLossAndRemainingQuantity(invoiceProductList);
            increaseProductRemainingQuantity(invoiceProductList);
        }

        invoiceToApprove.setInvoiceStatus(InvoiceStatus.APPROVED);

        invoiceRepository.save(invoiceToApprove);
    }

    private boolean validateInvoiceProducts(List<InvoiceProductDTO> invoiceProductList, Long invoiceId) {
        invoiceProductList.stream().forEach(invoiceProduct -> {
            Integer invoiceProductQuantity = invoiceProduct.getQuantity();
            Integer quantityInStock = invoiceProduct.getProduct().getQuantityInStock();
            if (quantityInStock < invoiceProductQuantity) {
                invoiceProductService.removeInvoiceProductFromInvoice(invoiceId, invoiceProduct.getId());
                throw new NoEnoughStockException("Your invoice can not be approved! Product do not have enough stock: " + invoiceProduct.getProduct().getName() + ". Product removed from invoice.");
            }
        });
        return true;
    }
    private void savePurchaseInvoiceProductProfitLossAndRemainingQuantity(List<InvoiceProductDTO> invoiceProductDTOList){
        invoiceProductDTOList.forEach(invPro -> {
            invPro.setProfitLoss(BigDecimal.ZERO);
            invPro.setRemainingQuantity(invPro.getQuantity());
            invoiceProductService.save(invPro);
        });
    }
    private void saveSalesInvoiceProductProfitLossAndRemainingQuantity(List<InvoiceProductDTO> invoiceProductDTOList){
        invoiceProductDTOList.forEach(invPro -> {
            // set the remainingQuantity, profitLoss and save
                    invPro.setRemainingQuantity(0);
                    invoiceProductService.save(invPro);
            // meantime calculate the profitLoss and remainingQuantity of products from evey previous Perches
            calculateTotalPerchesRemainingQuanProfitLossAndSave(invPro);

                });
    }

    private void decreaseProductRemainingQuantity(List<InvoiceProductDTO> invoiceProductList) {
        invoiceProductList.forEach(invoiceProductDTO -> {
            ProductDTO product = invoiceProductDTO.getProduct();
            Integer quantity = invoiceProductDTO.getQuantity();
            productService.decreaseProductQuantityInStock(product.getId(), quantity);
        });
    }

    private void increaseProductRemainingQuantity(List<InvoiceProductDTO> invoiceProductList) {
        invoiceProductList.forEach(invoiceProductDTO -> {
            ProductDTO product = invoiceProductDTO.getProduct();
            Integer quantity = invoiceProductDTO.getQuantity();
            productService.increaseProductQuantityInStock(product.getId(), quantity);
        });
    }
    @Transactional
    public void calculateTotalPerchesRemainingQuanProfitLossAndSave(InvoiceProductDTO invPro){
        // get the list of perchesInvoices invoiceProductList
        String companyName = invPro.getInvoice().getCompany().getTitle();
        String productName = invPro.getProduct().getName();

        List<InvoiceProductDTO> invoiceProducts = invoiceProductService
                .getPerchesInvoiceProductsListQuantityNotZero(
                        companyName,productName,InvoiceType.PURCHASE, 0);

        int salesQuantity = invPro.getQuantity();

        for (InvoiceProductDTO invoiceProduct : invoiceProducts) {
            int currentRemainingQuantity = invoiceProduct.getRemainingQuantity();
            int updatedRemainingQuantity = Math.max(currentRemainingQuantity - salesQuantity, 0);

            invoiceProduct.setRemainingQuantity(updatedRemainingQuantity);
            invoiceProductService.save(invoiceProduct);

//          sold quantity from remaining quantity
            int soldQuantity = currentRemainingQuantity-updatedRemainingQuantity;

            salesQuantity -= soldQuantity;

            if (salesQuantity <= 0) {
                // calculate the profitLoss of perches and save the remaining quantity and profitLoss to dataBase
                // then exit the loop
                calculateProfitLossAndSaveToDatabase(invPro,invoiceProduct,soldQuantity);
                break;
            }
            // calculate the profitLoss of perches and save the remaining quantity and profitLoss to dataBase
            calculateProfitLossAndSaveToDatabase(invPro,invoiceProduct,soldQuantity);
        }
    }

    private void calculateProfitLossAndSaveToDatabase(InvoiceProductDTO sales, InvoiceProductDTO perches, int soldQuantity){
        // calculate the product perches totalPrice
        BigDecimal totalPerches = calculateTotal(perches.getPrice(),
                soldQuantity,perches.getTax());

        // calculate sales totalPrice
        BigDecimal totalSale = calculateTotal(sales.getPrice(),
                soldQuantity,sales.getTax());

        // calculate the profitLoss and save to database than exit the loop
        sales.setProfitLoss(totalSale.subtract(totalPerches));
        invoiceProductService.save(sales);
    }

    private BigDecimal calculateTotal(BigDecimal price, int quantity, int taxRate){
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal taxRateMod = BigDecimal.valueOf(taxRate)
                .divide(PERCENTAGE_DIVISOR,2,RoundingMode.HALF_UP);
        return totalPrice.add(totalPrice.multiply(taxRateMod));
    }

    /**
     * Creates a invoice without saving it to database. InvoiceNo and Invoice date will be auto generated.
     *
     * @param invoiceType
     * @return
     */
    //Invoice_No should be auto generated
    //Invoice_Date should be the date which this invoice is created
    @Override
    public InvoiceDTO invoiceGenerator(InvoiceType invoiceType) {
        String companyTitle = securityService.getLoggedInUser().getCompany().getTitle();

        // Get the latest invoice from the database which belongs to that company
        Optional<Invoice> latestInvoice = invoiceRepository.findTopByCompany_TitleAndInvoiceTypeOrderByInvoiceNoDesc(companyTitle, invoiceType);

        // Generate the new invoice number
        String generatedInvoiceNo = generateNextInvoiceNumber(latestInvoice, invoiceType);

        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setInvoiceNo(generatedInvoiceNo);
        invoiceDTO.setDate(LocalDate.now());
        invoiceDTO.setInvoiceType(invoiceType);

        return invoiceDTO;
    }

    /**
     * Auto generate method for invoiceNo. Generates next invoiceNo based on last created invoice of a company even if it is deleted.
     *
     * @param lastInvoice
     * @param invoiceType
     * @return generated invoice bo
     */
    private String generateNextInvoiceNumber(Optional<Invoice> lastInvoice, InvoiceType invoiceType) {
        if (!lastInvoice.isPresent()) {
            return invoiceType.getValue().charAt(0) + "-001";
        }

        String lastInvoiceNumber = lastInvoice.get().getInvoiceNo();
        int lastNumber = Integer.parseInt(lastInvoiceNumber.substring(2));
        int nextNumber = lastNumber + 1;

        return invoiceType.getValue().charAt(0) + "-" + String.format("%03d", nextNumber);
    }

    /**
     * Creates invoice. InvoiceNo, invoice date and invoice type should be auto-generated before this method.
     *
     * @param invoice
     * @param invoiceType
     * @return created invoiceDTO
     */
    //InvoiceStatus should be AWAITING_APPROVAL
    //Company should be assigned here
    @Override
    public InvoiceDTO create(InvoiceDTO invoice, InvoiceType invoiceType) {
        CompanyDTO company = securityService.getLoggedInUser().getCompany();

        invoice.setInvoiceStatus(InvoiceStatus.AWAITING_APPROVAL);
        invoice.setCompany(company);
        invoice.setInvoiceType(invoiceType);

        Invoice invoiceToCreate = mapper.convert(invoice, new Invoice());
        Invoice savedInvoice = invoiceRepository.save(invoiceToCreate);

        return mapper.convert(savedInvoice, new InvoiceDTO());
    }


    public List<InvoiceDTO> findTop3ByCompanyOrderByDateDesc() {

        //Get the currently logged-in user
        CompanyDTO logInUser = securityService.getLoggedInUser().getCompany();

        //Get the company of the logged-in user
        Company company = mapper.convert(logInUser, new Company());

        // Query the database to find the top 3 invoices based on certain criteria
        List<InvoiceDTO> invoiceDTOList = invoiceRepository.findTop3ByCompanyAndInvoiceStatusAndIsDeletedOrderByDateDesc(company, InvoiceStatus.APPROVED, false).stream()
                .map(invoice -> {
                    InvoiceDTO invoiceDTO = mapper.convert(invoice, new InvoiceDTO());
                    return invoiceDTO;
                })
                .map(invoiceDTO -> {
                    // Retrieve the list of InvoiceProductDTO objects associated with the given invoiceDTO.
                    List<InvoiceProductDTO> invoiceProductsList = invoiceProductService.findByInvoiceId(invoiceDTO.getId());

                    //Calculate the taxAmount, totalPriceWithoutTax , totalPrice
                    BigDecimal taxAmount = calculateTax(invoiceProductsList);
                    BigDecimal totalPriceWithoutTax = calculateTotalPriceWithoutTax(invoiceProductsList);
                    BigDecimal totalPrice = taxAmount.add(totalPriceWithoutTax);

                    // Update the InvoiceDTO with the calculated price, tax, and total.
                    invoiceDTO.setTax(taxAmount);
                    invoiceDTO.setPrice(totalPriceWithoutTax);
                    invoiceDTO.setTotal(totalPrice);
                    return invoiceDTO;

                })
                .collect(Collectors.toList());

        return invoiceDTOList;


    }

    @Override
    public boolean existsByClientVendorId(Long id) {
        return invoiceRepository.existsByClientVendorId(id);

    }
}
