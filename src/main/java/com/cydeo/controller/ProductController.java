package com.cydeo.controller;

import com.cydeo.dto.ProductDTO;
import com.cydeo.enums.ProductUnit;
import com.cydeo.repository.ProductRepository;
import com.cydeo.service.CategoryService;
import com.cydeo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;


    //End-user should be able to List (display) all products in the product_list page...
    @GetMapping("/list")
    public String listAllProducts(Model model){
        List<ProductDTO> productDTOList = productService.listAllProducts();
        model.addAttribute("products", productDTOList);

        return "/product/product-list";
    }

    //End-user should be able to Edit each product, when click on Edit button, end-user should land on product_update page and the edit form should be populated with the information of that very same product.
    @GetMapping("/update/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model){
        ProductDTO productToBeUpdated = productService.findById(id);
        model.addAttribute("product",productToBeUpdated);
        model.addAttribute("categories", categoryService.listAllCategories());
        model.addAttribute("lowLimitAlert",productToBeUpdated.getLowLimitAlert());
        model.addAttribute("productUnits", List.of(ProductUnit.values()));
        return "/product/product-update";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@Valid @ModelAttribute("product") ProductDTO product,
                                BindingResult bindingResult, Model model) {
        bindingResult = productService.addUpdateProductNameValidation(product,bindingResult);
        if (bindingResult.hasFieldErrors()){
            model.addAttribute("categories", categoryService.listAllCategories());
            model.addAttribute("productUnits", List.of(ProductUnit.values()));
            return "/product/product-update";
        }
        productService.update(product);
        return "redirect:/products/list";
    }


    //When End-User clicks on "Create-Product" button, product_create page should be displayed with an Empty product form,
    @GetMapping("/create")
    public String createProduct(Model model){
        model.addAttribute("newProduct", new ProductDTO());
        model.addAttribute("categories",categoryService.listAllCategories());
        model.addAttribute("productUnits", List.of(ProductUnit.values()));
        return "/product/product-create";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute("newProduct") ProductDTO productDTO,
                                BindingResult bindingResult, Model model){
        bindingResult = productService.addProductNameValidation(productDTO,bindingResult);
        if (bindingResult.hasFieldErrors()){
            model.addAttribute("categories",categoryService.listAllCategories());
            model.addAttribute("productUnits", List.of(ProductUnit.values()));
            return "/product/product-create";

        }

        productService.save(productDTO);

        return "redirect:/products/list";
    }

    //End-user should be able to Delete each product(soft delete), then end up to the product_list page with updated product list.
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id){
        productService.delete(id);

        return "redirect:/products/list";
    }



}