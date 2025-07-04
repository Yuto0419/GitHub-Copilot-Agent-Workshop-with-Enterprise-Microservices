package com.skishop.frontend.controller;

import com.skishop.frontend.dto.CategoryDto;
import com.skishop.frontend.dto.ProductDto;
import com.skishop.frontend.dto.ProductSearchResponse;
import com.skishop.frontend.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Home page and main page controller
 */
@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Display home page
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        try {
            // Get recommended products (synchronous execution)
            ProductSearchResponse featuredResponse = productService.getProducts(0, 8, "rating", null, null)
                    .block();
            ProductSearchResponse newResponse = productService.getProducts(0, 8, "createdAt", null, null)
                    .block();
            List<CategoryDto> categoryList = productService.getCategories()
                    .block();

            // Add to model (with null check)
            model.addAttribute("featuredProducts", 
                featuredResponse != null && featuredResponse.products() != null ? 
                    featuredResponse.products() : new ArrayList<>());
            model.addAttribute("newProducts", 
                newResponse != null && newResponse.products() != null ? 
                    newResponse.products() : new ArrayList<>());
            model.addAttribute("categories", 
                categoryList != null ? categoryList : new ArrayList<>());

        } catch (Exception e) {
            // Fallback when error occurs
            model.addAttribute("featuredProducts", new ArrayList<>());
            model.addAttribute("newProducts", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
        }

        model.addAttribute("pageTitle", "Azure SkiShop - Supporting Your Ski Life");
        model.addAttribute("isHomePage", true);
        model.addAttribute("isAdminPage", false);
        
        // Alert variables initialization
        model.addAttribute("success", "");
        model.addAttribute("error", "");
        model.addAttribute("info", "");
        model.addAttribute("warning", "");

        return "index";
    }

    /**
     * Product list page
     */
    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            Model model) {

        try {
            // Execute Mono synchronously and get result
            ProductSearchResponse response = productService.getProducts(page, size, sort, category, brand)
                    .block(); // Synchronous execution
            
            List<CategoryDto> categoryList = productService.getCategories()
                    .block(); // Synchronous execution

            // Set attributes only when response is not null
            if (response != null) {
                model.addAttribute("products", response.products() != null ? response.products() : new ArrayList<>());
                model.addAttribute("totalPages", (response.totalCount() + size - 1) / size);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalProducts", response.totalCount());
            } else {
                // Default values when data cannot be retrieved from API
                model.addAttribute("products", new ArrayList<>());
                model.addAttribute("totalPages", 0);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalProducts", 0);
            }

            // Also null check for categories
            model.addAttribute("categories", categoryList != null ? categoryList : new ArrayList<>());

        } catch (Exception e) {
            // Fallback when error occurs
            model.addAttribute("products", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalProducts", 0);
        }

        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("currentSort", sort);
        model.addAttribute("pageTitle", "Product List - Azure SkiShop");
        model.addAttribute("isAdminPage", false);
        
        // Alert variables initialization
        model.addAttribute("success", "");
        model.addAttribute("error", "");
        model.addAttribute("info", "");
        model.addAttribute("warning", "");

        return "products/list";
    }

    /**
     * Product detail page
     */
    @GetMapping("/products/{id}")
    public String productDetail(@org.springframework.web.bind.annotation.PathVariable String id, Model model) {
        try {
            // Get product details with synchronous execution
            ProductDto product = productService.getProduct(id).block();
            List<ProductDto> relatedProducts = productService.getRelatedProducts(id).block();

            if (product != null) {
                model.addAttribute("product", product);
                model.addAttribute("pageTitle", product.name() + " - Azure SkiShop");
            } else {
                model.addAttribute("pageTitle", "Product Detail - Azure SkiShop");
            }

            model.addAttribute("relatedProducts", 
                relatedProducts != null ? relatedProducts : new ArrayList<>());

        } catch (Exception e) {
            // Fallback when error occurs
            model.addAttribute("relatedProducts", new ArrayList<>());
            model.addAttribute("pageTitle", "Product Detail - Azure SkiShop");
        }

        model.addAttribute("isAdminPage", false);
        
        // Alert variables initialization
        model.addAttribute("success", "");
        model.addAttribute("error", "");
        model.addAttribute("info", "");
        model.addAttribute("warning", "");
        
        return "products/detail";
    }

    /**
     * Product search
     */
    @GetMapping("/search")
    public String search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        try {
            // Get search results with synchronous execution
            ProductSearchResponse response = productService.searchProducts(q, page, size).block();

            if (response != null) {
                model.addAttribute("products", response.products() != null ? response.products() : new ArrayList<>());
                model.addAttribute("totalPages", (response.totalCount() + size - 1) / size);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalProducts", response.totalCount());
            } else {
                // Default values when search results cannot be retrieved
                model.addAttribute("products", new ArrayList<>());
                model.addAttribute("totalPages", 0);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalProducts", 0);
            }

        } catch (Exception e) {
            // Fallback when error occurs
            model.addAttribute("products", new ArrayList<>());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalProducts", 0);
        }

        model.addAttribute("searchQuery", q);
        model.addAttribute("pageTitle", "Search Results: " + q + " - Azure SkiShop");
        model.addAttribute("isAdminPage", false);
        
        // Alert variables initialization
        model.addAttribute("success", "");
        model.addAttribute("error", "");
        model.addAttribute("info", "");
        model.addAttribute("warning", "");

        return "products/search-results";
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login - Azure SkiShop");
        model.addAttribute("isAdminPage", false);
        
        // Alert variables initialization
        model.addAttribute("success", "");
        model.addAttribute("error", "");
        model.addAttribute("info", "");
        model.addAttribute("warning", "");
        
        return "auth/login";
    }
}
