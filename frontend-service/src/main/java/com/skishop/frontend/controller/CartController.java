package com.skishop.frontend.controller;

import com.skishop.frontend.dto.AddToCartRequest;
import com.skishop.frontend.dto.CartDto;
import com.skishop.frontend.service.CartService;
import com.skishop.frontend.service.CartService.CartServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for cart-related operations.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    
    @Value("${app.skishop.auth.enabled:true}")
    private boolean authEnabled;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Get user ID (regardless of authentication status)
     */
    private String getUserId(Authentication auth) {
        if (authEnabled && auth != null) {
            return auth.getName();
        }
        // Default user ID for testing
        return "test-user-1";
    }

    /**
     * Display cart
     */
    @GetMapping
    public String viewCart(Authentication auth, Model model) {
        String userId = getUserId(auth);
        try {
            CartDto cart = cartService.getCart(userId);
            if (cart != null) {
                model.addAttribute("cart", cart);
            }
        } catch (CartServiceUnavailableException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred while retrieving the cart.");
        }

        model.addAttribute("pageTitle", "Cart - Azure SkiShop");
        return "cart/view";
    }

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String userId = getUserId(auth);
        AddToCartRequest request = new AddToCartRequest(productId, quantity);

        try {
            cartService.addToCart(userId, request);
            redirectAttributes.addFlashAttribute("success", "Item added to cart");
        } catch (CartServiceUnavailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while adding to cart.");
        }

        return "redirect:/cart";
    }

    /**
     * Update cart item
     */
    @PostMapping("/update")
    public String updateCartItem(
            @RequestParam String itemId,
            @RequestParam int quantity,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String userId = getUserId(auth);

        try {
            cartService.updateCartItem(userId, itemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated");
        } catch (CartServiceUnavailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while updating the cart.");
        }

        return "redirect:/cart";
    }

    /**
     * Remove cart item
     */
    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam String itemId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String userId = getUserId(auth);

        try {
            cartService.removeFromCart(userId, itemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (CartServiceUnavailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while removing the item.");
        }

        return "redirect:/cart";
    }

    /**
     * Clear cart
     */
    @PostMapping("/clear")
    public String clearCart(Authentication auth, RedirectAttributes redirectAttributes) {
        String userId = getUserId(auth);

        try {
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("success", "Cart cleared");
        } catch (CartServiceUnavailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while clearing the cart.");
        }

        return "redirect:/cart";
    }

    /**
     * AJAX: Get cart item count
     */
    @GetMapping("/count")
    @ResponseBody
    public Object getCartItemCount(Authentication auth) {
        String userId = getUserId(auth);
        try {
            CartDto cart = cartService.getCart(userId);
            int count = cart != null ? cart.itemCount() : 0;
            return java.util.Map.of("count", count);
        } catch (Exception e) {
            return java.util.Map.of("count", 0);
        }
    }
}
