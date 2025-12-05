package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Blog;
import org.fsm.entity.Blog.BlogStatus;
import org.fsm.entity.Order;
import org.fsm.entity.Product;
import org.fsm.entity.User;
import org.fsm.repository.BrandRepository;
import org.fsm.repository.CategoryRepository;
import org.fsm.repository.OrderRepository;
import org.fsm.repository.ProductRepository;
import org.fsm.repository.UserRepository;
import org.fsm.service.AuditLogService;
import org.fsm.service.BlogService;
import org.fsm.service.SessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class StaffController {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final BlogService blogService;

    @GetMapping("/staff")
    public String staff(
            Model model,
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int blogPage
    ) {
        // ===== Products / Brands / Categories cho staff.html =====
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);

        model.addAttribute("brands", brandRepository.findAll());

        // Categories - QUAN TRỌNG → để Category ở staff.html có data
        model.addAttribute("categories", categoryRepository.findAll());

        // ===== Orders - Load all orders sorted by createdAt descending =====
        List<Order> orders = orderRepository.findAll(Sort.by("createdAt").descending());
        model.addAttribute("orders", orders);

        // ===== Audit logs (nếu staff.html có dùng) =====
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var auditLogs = auditLogService.getAllAuditLogs(pageable);
        model.addAttribute("auditLogs", auditLogs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());

        // ===== Current staff info (nếu cần show trong header) =====
        Long currentUserId = sessionService.getCurrentUserId(session);
        model.addAttribute("currentUserId", currentUserId);

        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            model.addAttribute("currentUser", currentUser);
            
            // ===== Blogs của staff với phân trang =====
            Page<Blog> blogPageResult;
            int blogPageSize = "blogs".equals(tab) ? 5 : 50; // 5 blogs per page cho tab blogs
            
            if ("blogs".equals(tab)) {
                Pageable blogPageable = PageRequest.of(blogPage, blogPageSize, Sort.by("createdAt").descending());
                
                // Nếu có keyword, search
                if (keyword != null && !keyword.trim().isEmpty()) {
                    blogPageResult = blogService.searchBlogsByCreator(currentUserId, keyword.trim(), blogPageable);
                }
                // Nếu có status filter
                else if (status != null && !status.isEmpty()) {
                    try {
                        BlogStatus blogStatus = BlogStatus.valueOf(status.toUpperCase());
                        blogPageResult = blogService.getBlogsByCreatorAndStatus(currentUserId, blogStatus, blogPageable);
                    } catch (IllegalArgumentException e) {
                        blogPageResult = blogService.getBlogsByCreator(currentUserId, blogPageable);
                    }
                }
                // Lấy tất cả blog của staff
                else {
                    blogPageResult = blogService.getBlogsByCreator(currentUserId, blogPageable);
                }
                
                model.addAttribute("blogs", blogPageResult.getContent());
                model.addAttribute("blogCurrentPage", blogPageResult.getNumber());
                model.addAttribute("blogTotalPages", blogPageResult.getTotalPages());
                model.addAttribute("blogTotalElements", blogPageResult.getTotalElements());
                model.addAttribute("blogPageSize", blogPageSize);
            } else {
                // Nếu không phải tab blogs, lấy tất cả (không phân trang) để tính stats
                List<Blog> allBlogs = blogService.getBlogsByCreator(currentUserId);
                model.addAttribute("blogs", allBlogs);
            }
            
            model.addAttribute("blogStatuses", BlogStatus.values());
            model.addAttribute("currentBlogStatus", status);
            model.addAttribute("blogKeyword", keyword);
            
            // Blog stats (tính từ tất cả blogs của staff)
            model.addAttribute("totalBlogs", blogService.countBlogsByCreator(currentUserId));
            model.addAttribute("draftBlogCount", blogService.getBlogsByStatus(BlogStatus.DRAFT)
                    .stream().filter(b -> b.getCreatedBy().equals(currentUserId)).count());
            model.addAttribute("pendingBlogCount", blogService.getBlogsByStatus(BlogStatus.PENDING_REVIEW)
                    .stream().filter(b -> b.getCreatedBy().equals(currentUserId)).count());
            model.addAttribute("publishedBlogCount", blogService.getBlogsByStatus(BlogStatus.PUBLISHED)
                    .stream().filter(b -> b.getCreatedBy().equals(currentUserId)).count());
        }
        
        // Set active tab nếu có
        if (tab != null && !tab.isEmpty()) {
            model.addAttribute("activeTab", tab);
        }

        // Trả về staff.html
        return "staff";
    }

    @GetMapping("/staff/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Product stats
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getActive() != null && p.getActive())
                .count();
        long inactiveProducts = totalProducts - activeProducts;
        
        // Brand stats
        long totalBrands = brandRepository.count();
        
        // Order stats
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus("PENDING") + orderRepository.countByStatus("COD_PENDING");
        long completedOrders = orderRepository.countByStatus("PAID") + orderRepository.countByStatus("CONFIRMED") + orderRepository.countByStatus("DELIVERED");
        
        // Blog stats
        long totalBlogs = blogService.getAllBlogsForStaff().size();
        
        stats.put("products", Map.of(
                "total", totalProducts,
                "active", activeProducts,
                "inactive", inactiveProducts
        ));
        
        stats.put("brands", Map.of("total", totalBrands));
        
        stats.put("orders", Map.of(
                "total", totalOrders,
                "pending", pendingOrders,
                "completed", completedOrders
        ));
        
        stats.put("blogs", Map.of("total", totalBlogs));
        
        return ResponseEntity.ok(stats);
    }
}