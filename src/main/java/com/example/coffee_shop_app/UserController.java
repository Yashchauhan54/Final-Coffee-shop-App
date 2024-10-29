package com.example.coffee_shop_app;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private ProductService productService; // Service for managing products



    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("products", productService.getAllProducts()); // List all available products
            return "redirect:/"; // Redirect to homepage if user is logged in
        } else {
            return "redirect:/login"; // Redirect to login if user is not logged in
        }
    }

    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Product product = productService.getProductById(id); // Fetch product by ID
            if (product == null) {
                return "redirect:/dashboard"; // If the product doesn't exist, go back to dashboard
            }
            model.addAttribute("product", product);
            model.addAttribute("user", user);
            return "product-list"; // View showing product details
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return "redirect:/dashboard"; // If the product doesn't exist, return to dashboard
            }

            // Logic for adding product to cart (adjust accordingly if cart exists)

            model.addAttribute("user", user);
            return "redirect:/dashboard";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/order-history")
    public String viewOrderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            // Since orders are removed, adjust this logic if you introduce a separate order system
            return "order-history"; // View showing order history, if applicable
        } else {
            return "redirect:/login";
        }
    }





}
