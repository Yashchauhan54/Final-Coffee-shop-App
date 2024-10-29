package com.example.coffee_shop_app;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("admin/")
public class AdminController {

    @Autowired
    private ProductService productService; // Service to manage products

    private static final String UPLOAD_DIR = "src/main/resources/static/images/"; // Directory to store product images

    @GetMapping("products")
    public String getAllProducts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getType().equals("admin")) {
            List<Product> products = productService.getAllProducts();
            model.addAttribute("products", products);
            model.addAttribute("user", user);
            return "product-list"; // View displaying all products
        } else {
            return "redirect:/"; // Redirect to homepage if not an admin
        }
    }

    @GetMapping("add-product")
    public String addProductForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getType().equals("admin")) {
            model.addAttribute("product", new Product()); // Form to add a new product
            model.addAttribute("user", user);
            return "add-product"; // View for adding new product
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("add-product")
    public String addProduct(@RequestParam String productName,
                             @RequestParam String description,
                             @RequestParam double price,
                             @RequestParam MultipartFile imageFile,
                             HttpSession session, Model model) throws IOException {

        User user = (User) session.getAttribute("user");
        if (user != null && user.getType().equals("admin")) {
            String imageName = imageFile.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + imageName);
            Files.createDirectories(path.getParent()); // Ensure the directory exists
            Files.write(path, imageFile.getBytes());

            Product product = new Product();
            product.setProductName(productName);
            product.setDescription(description);
            product.setPrice(price);
            product.setImage(imageName); // Store the image name in the product

            productService.saveProduct(product); // Save the new product to the database
            return "redirect:/admin/products"; // Redirect to product list after adding
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("update-product")
    public String updateProductForm(@RequestParam Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getType().equals("admin")) {
            Product product = productService.getProductById(id);
            if (product == null) {
                return "redirect:/admin/products"; // Handle case where product is not found
            }
            model.addAttribute("product", product); // Pass the product to the update form
            model.addAttribute("user", user);
            return "add-product"; // Reuse the form view for updating the product
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("update-product")
    public String updateProduct(@RequestParam Long productId,
                                @RequestParam String productName,
                                @RequestParam String description,
                                @RequestParam double price,
                                @RequestParam(required = false) MultipartFile imageFile,
                                HttpSession session, Model model) throws IOException {

        User user = (User) session.getAttribute("user");
        if (user != null && user.getType().equals("admin")) {
            Product existingProduct = productService.getProductById(productId);
            if (existingProduct == null) {
                return "redirect:/admin/products"; // Handle case where product is not found
            }

            existingProduct.setProductName(productName);
            existingProduct.setDescription(description);
            existingProduct.setPrice(price);

            // Check if a new image is uploaded
            if (imageFile != null && !imageFile.isEmpty()) {
                String oldImageName = existingProduct.getImage();
                Path oldImagePath = Paths.get(UPLOAD_DIR + oldImageName);
                if (Files.exists(oldImagePath)) {
                    Files.delete(oldImagePath); // Delete the old image file
                }
                String newImageName = imageFile.getOriginalFilename();
                Path newPath = Paths.get(UPLOAD_DIR + newImageName);
                Files.write(newPath, imageFile.getBytes()); // Save the new image
                existingProduct.setImage(newImageName); // Update with the new image name
            }

            productService.saveProduct(existingProduct); // Save the updated product
            return "redirect:/admin/products";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("delete-product")
    public String deleteProduct(@RequestParam Long id, HttpSession session, Model model) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getType().equals("admin")) {
            Product product = productService.getProductById(id);
            if (product == null) {
                return "redirect:/admin/products"; // Handle case where product is not found
            }
            String imageName = product.getImage();
            Path imagePath = Paths.get(UPLOAD_DIR + imageName);
            if (Files.exists(imagePath)) {
                Files.delete(imagePath); // Delete the product's image file
            }
            productService.deleteProduct(id); // Delete the product from the database
            return "redirect:/admin/products";
        } else {
            return "redirect:/";
        }
    }


    @Autowired
    private OrderRepository orderRepository;

    // Method to display all orders (as before)
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        model.addAttribute("orders", orders);
        return "admin-order-history"; // Thymeleaf template for displaying all orders
    }


    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @GetMapping("/order-details/{orderId}")
    public String orderDetails(@PathVariable Long orderId, Model model) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + orderId));
        List<OrderHistory> orderHistoryList = orderHistoryRepository.findByOrder(order);

        model.addAttribute("orderHistoryList", orderHistoryList);
        model.addAttribute("order", order);
        return "order-details";
    }


    @PostMapping("/order/update")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") String status) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
        return "redirect:/admin/orders"; // Redirect to the orders page after updating
    }
}
