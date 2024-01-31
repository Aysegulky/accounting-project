package com.cydeo.controller;

import com.cydeo.dto.ProductDTO;
import com.cydeo.entity.*;
import com.cydeo.entity.common.UserPrincipal;
import com.cydeo.enums.ProductUnit;
import com.cydeo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Company company;
    private UserPrincipal principal;
    private Product product;
    private Category category;

    @BeforeEach
    void setUp(){
        Role role = new Role("Admin");
        role = roleRepository.save(role);

        company = new Company();
        company.setTitle("Company A");
        companyRepository.save(company);

        category = new Category();
        category.setDescription("Category");
        category.setCompany(company);
        category = categoryRepository.save(category);

        product = new Product();
        product.setName("Product");
        product.setCategory(category);
        product.setLowLimitAlert(10);
        product.setProductUnit(ProductUnit.PCS);
        product = productRepository.save(product);

        user = new User();
        user.setUsername("username");
        user.setRole(role);
        user.setEnabled(true);
        user.setIsDeleted(false);
        user.setPassword("Abc1");
        user.setCompany(company);
        user = userRepository.save(user);

        principal = new UserPrincipal(user);
    }

    //@GetMapping("/list")
    @Test
    void should_retrieve_product_list_page_and_display_all_products() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/products/list")
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("/product/product-list"))
                .andExpect(model().attributeExists("products"));

    }

    //@GetMapping("/update/{id}")
    @Test
    void should_return_update_page() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/products/update/"+product.getId())
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("/product/product-update"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("lowLimitAlert"))
                .andExpect(model().attributeExists("productUnits"));
    }

    //@PostMapping("/update/{id}")
    @Test
    void should_update_product_successfully_and_redirect() throws Exception {
        product.setName("Update Product");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", product.getName());
        formData.add("category", String.valueOf(product.getCategory().getId()));
        formData.add("lowLimitAlert", String.valueOf(product.getLowLimitAlert()));
        formData.add("productUnit", product.getProductUnit().name());

        mvc.perform(MockMvcRequestBuilders.post("/products/update/" + product.getId())
                        .params(formData)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/list"));

        assertThat(product.getName()).isEqualTo("Update Product");
    }

    //@GetMapping("/create")
    @Test
    void should_retrieve_create_page() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/products/create")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("/product/product-create"))
                .andExpect(model().attributeExists("newProduct"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("productUnits"))
                .andExpect(model().attribute("newProduct", instanceOf(ProductDTO.class)))
                .andExpect(model().attribute("productUnits", hasItems(ProductUnit.LBS, ProductUnit.GALLON, ProductUnit.PCS, ProductUnit.KG, ProductUnit.METER, ProductUnit.INCH, ProductUnit.FEET)));

    }

    //@PostMapping("/create")
    @Test
    void should_create_product_object() throws Exception {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "new product");
        formData.add("category", String.valueOf(category.id));
        formData.add("lowLimitAlert", String.valueOf(10));
        formData.add("productUnit", ProductUnit.PCS.name());
        formData.add("quantityInStock", String.valueOf(0));

        mvc.perform(MockMvcRequestBuilders.post("/products/create")
                .params(formData)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/list"));

        List<Product> allProducts = productRepository.findAll();
        List<String> allProductsNames = allProducts.stream().map(Product::getName).collect(Collectors.toList());

        assertThat(allProductsNames.contains("new product")).isTrue();
    }

    //@GetMapping("/delete/{id}")
    @Test
    void should_delete_existing_product_with_id() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/products/delete/" + product.id)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/list"));

        Optional<Product> deletedProduct = productRepository.findById(product.id);//should return null since entity has @Where(is_deleted=false)

        assertThat(deletedProduct).isNotPresent();
    }

}