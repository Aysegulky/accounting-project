package com.cydeo.service.impl;

import com.cydeo.dto.CompanyDTO;
import com.cydeo.dto.ProductDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Category;
import com.cydeo.entity.Company;
import com.cydeo.entity.Product;
import com.cydeo.entity.User;
import com.cydeo.entity.common.UserPrincipal;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repository.CategoryRepository;
import com.cydeo.repository.CompanyRepository;
import com.cydeo.repository.ProductRepository;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.InvoiceProductService;
import com.cydeo.service.ProductService;
import com.cydeo.service.SecurityService;
import com.cydeo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ProductServiceImplIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MapperUtil mapperUtil;

    //under test
    @Autowired
    private ProductService productService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    Company relatedCompany;
    Company nonRelatedCompany;

    Category relatedCategory;
    Category nonRelatedCategory;

    Product relatedProduct;
    Product nonRelatedProduct;

    @BeforeEach
    void setUp() {
        relatedCompany = new Company();
        relatedCompany = companyRepository.save(relatedCompany);

        nonRelatedCompany = new Company();
        nonRelatedCompany = companyRepository.save(nonRelatedCompany);

        relatedCategory = new Category();
        relatedCategory.setCompany(relatedCompany);
        relatedCategory = categoryRepository.save(relatedCategory);

        nonRelatedCategory = new Category();
        nonRelatedCategory.setCompany(nonRelatedCompany);
        nonRelatedCategory = categoryRepository.save(nonRelatedCategory);

        relatedProduct = new Product();
        relatedProduct.setCategory(relatedCategory);
        relatedProduct.setName("relatedProduct");
        relatedProduct = productRepository.save(relatedProduct);

        nonRelatedProduct = new Product();
        nonRelatedProduct.setCategory(nonRelatedCategory);
        nonRelatedProduct = productRepository.save(nonRelatedProduct);

        User user = new User();
        user.setUsername("dummyUser");
        user.setCompany(relatedCompany);
        userRepository.save(mapperUtil.convert(user, new User()));
        UserServiceImpl userService1 = mock(UserServiceImpl.class);
        when(userService1.findByUsername("dummyUser")).thenReturn(mapperUtil.convert(user, new UserDTO()));

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("dummyUser");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

    }

    /*
     ***************** findById() *****************
     */
    @Test
    void should_find_product_from_id() {
        ProductDTO found = productService.findById(relatedProduct.id);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("relatedProduct");
    }

    /*
     ***************** listAllProducts() *****************
     */
    @Test
    void should_find_all_products_related_to_company() {

        List<ProductDTO> productDTOList = productService.listAllProducts();

        assertThat(productDTOList).isNotEmpty();
        assertThat(productDTOList).hasSize(1);
        assertThat(productDTOList.get(0).getName()).isEqualTo("relatedProduct");
    }

    /*
     ***************** save() *****************
     */
    @Test
    void should_save_product_to_db(){
        ProductDTO productToSave = new ProductDTO();
        productToSave.setName("product name");

        productService.save(productToSave);

        List<Product> allProducts = productRepository.findAll();
        List<String> allProductNames = allProducts.stream().map(product -> product.getName()).collect(Collectors.toList());

        assertTrue(allProductNames.contains(productToSave.getName()));
    }

    /*
     ***************** update() *****************
     */
    @Test
    void should_update_product(){
        relatedProduct.setName("updated product");
        ProductDTO convertedProduct = mapperUtil.convert(relatedProduct, new ProductDTO());

        productService.update(convertedProduct);

        String updatedName = productRepository.findById(relatedProduct.id).get().getName();

        assertThat(updatedName).isEqualTo("updated product");
    }

    /*
     ***************** delete() *****************
     */
    @Test
    void should_delete_product(){
        Product product = new Product();
        product.setQuantityInStock(0);
        product = productRepository.save(product);

        productService.delete(product.id);

        Optional<Product> deletedProduct = productRepository.findById(product.id);//should return empty since @Where(is_deleted=false)
        assertThat(deletedProduct).isNotPresent();
    }

    /*
     ***************** decreaseProductQuantityInStock() *****************
     */
    @Test
    void should_decrease_product_stock(){
        relatedProduct.setQuantityInStock(100);

        productService.decreaseProductQuantityInStock(relatedProduct.id, 50);

        int decreasedStock = productRepository.findById(relatedProduct.id).get().getQuantityInStock();
        assertThat(decreasedStock).isEqualTo(50);
    }

    /*
     ***************** increaseProductQuantityInStock() *****************
     */
    @Test
    void should_increase_product_stock(){
        relatedProduct.setQuantityInStock(100);

        productService.increaseProductQuantityInStock(relatedProduct.id, 50);

        int decreasedStock = productRepository.findById(relatedProduct.id).get().getQuantityInStock();
        assertThat(decreasedStock).isEqualTo(150);
    }

    /*
     ***************** findProductsByCompanyAndHaveStock() *****************
     */
    @Test
    void should_return_product_that_has_available_stock(){
        relatedProduct.setQuantityInStock(100);
        relatedProduct.setName("product");

        List<ProductDTO> productList = productService.findProductsByCompanyAndHaveStock(relatedCompany);
        List<String> productNames = productList.stream().map(ProductDTO::getName).collect(Collectors.toList());

        assertTrue(productNames.contains("product"));
    }

    @Test
    void should_return_product_that_has_not_available_stock(){
        relatedProduct.setQuantityInStock(0);
        relatedProduct.setName("product");

        List<ProductDTO> productList = productService.findProductsByCompanyAndHaveStock(relatedCompany);
        List<String> productNames = productList.stream().map(ProductDTO::getName).collect(Collectors.toList());

        assertFalse(productNames.contains("product"));
    }
}