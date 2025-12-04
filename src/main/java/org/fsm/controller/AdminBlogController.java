package org.fsm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.Blog;
import org.fsm.entity.Blog.BlogStatus;
import org.fsm.exception.ResourceNotFoundException;
import org.fsm.service.BlogService;
import org.fsm.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService blogService;
    private final SessionService sessionService;

    // ==================== VIEW METHODS ====================

    /**
     * Hiển thị danh sách tất cả blog (cho Admin)
     */
    @GetMapping
    public String listAllBlogs(
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session) {
        
        if (!sessionService.isAdmin(session)) {
            return "redirect:/login?error=unauthorized";
        }

        List<Blog> blogs;
        if (status != null && !status.isEmpty()) {
            try {
                BlogStatus blogStatus = BlogStatus.valueOf(status.toUpperCase());
                blogs = blogService.getBlogsByStatus(blogStatus);
            } catch (IllegalArgumentException e) {
                blogs = blogService.getAllBlogsForStaff();
            }
        } else {
            blogs = blogService.getAllBlogsForStaff();
        }

        model.addAttribute("blogs", blogs);
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", BlogStatus.values());
        
        // Thống kê
        model.addAttribute("totalBlogs", blogs.size());
        model.addAttribute("draftCount", blogService.countBlogsByStatus(BlogStatus.DRAFT));
        model.addAttribute("pendingCount", blogService.countBlogsByStatus(BlogStatus.PENDING_REVIEW));
        model.addAttribute("publishedCount", blogService.countBlogsByStatus(BlogStatus.PUBLISHED));

        return "admin/blog-list";
    }

    /**
     * Hiển thị danh sách blog chờ duyệt (PENDING_REVIEW)
     */
    @GetMapping("/pending")
    public String listPendingBlogs(Model model, HttpSession session) {
        if (!sessionService.isAdmin(session)) {
            return "redirect:/login?error=unauthorized";
        }

        List<Blog> pendingBlogs = blogService.getPendingBlogs();
        model.addAttribute("blogs", pendingBlogs);
        model.addAttribute("currentStatus", "PENDING_REVIEW");
        model.addAttribute("statuses", BlogStatus.values());
        
        // Thống kê
        model.addAttribute("totalBlogs", blogService.getAllBlogsForStaff().size());
        model.addAttribute("draftCount", blogService.countBlogsByStatus(BlogStatus.DRAFT));
        model.addAttribute("pendingCount", blogService.countBlogsByStatus(BlogStatus.PENDING_REVIEW));
        model.addAttribute("publishedCount", blogService.countBlogsByStatus(BlogStatus.PUBLISHED));

        return "admin/blog-list";
    }

    /**
     * Hiển thị chi tiết blog (cho Admin xem)
     */
    @GetMapping("/view/{id}")
    public String viewBlog(@PathVariable Long id, Model model, HttpSession session) {
        if (!sessionService.isAdmin(session)) {
            return "redirect:/login?error=unauthorized";
        }

        Blog blog = blogService.getBlogByIdForStaff(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

        model.addAttribute("blog", blog);
        model.addAttribute("userRole", "admin");
        
        // Tạo backUrl với status filter nếu có
        String backUrl = "/admin?tab=blogs";
        if (blog.getStatus() != null) {
            backUrl += "&status=" + blog.getStatus().name();
        }
        model.addAttribute("backUrl", backUrl);
        
        return "blog-detail-management";
    }

    // ==================== ACTION METHODS ====================

    /**
     * Duyệt blog (PENDING_REVIEW -> PUBLISHED) - AJAX endpoint
     */
    @PostMapping(value = "/approve/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> approveBlog(@PathVariable Long id, HttpSession session) {
        try {
            if (!sessionService.isAdmin(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Long adminId = sessionService.getCurrentUserId(session);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

            // Chỉ cho phép duyệt nếu status là PENDING_REVIEW
            if (blog.getStatus() != BlogStatus.PENDING_REVIEW) {
                return ResponseEntity.badRequest().body("Chỉ có thể duyệt blog ở trạng thái PENDING_REVIEW");
            }

            blogService.approveBlog(id, adminId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Blog approved and published successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Từ chối blog (PENDING_REVIEW -> DRAFT) - AJAX endpoint
     */
    @PostMapping(value = "/reject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> rejectBlog(@PathVariable Long id, HttpSession session) {
        try {
            if (!sessionService.isAdmin(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Long adminId = sessionService.getCurrentUserId(session);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

            // Chỉ cho phép từ chối nếu status là PENDING_REVIEW
            if (blog.getStatus() != BlogStatus.PENDING_REVIEW) {
                return ResponseEntity.badRequest().body("Chỉ có thể từ chối blog ở trạng thái PENDING_REVIEW");
            }

            blogService.rejectBlog(id, adminId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Blog rejected and returned to DRAFT"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Archive blog (PUBLISHED -> ARCHIVED) - AJAX endpoint
     */
    @PostMapping(value = "/archive/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> archiveBlog(@PathVariable Long id, HttpSession session) {
        try {
            if (!sessionService.isAdmin(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Long adminId = sessionService.getCurrentUserId(session);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            blogService.archiveBlog(id, adminId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Blog archived successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Unarchive blog (ARCHIVED -> PUBLISHED) - AJAX endpoint
     */
    @PostMapping(value = "/unarchive/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> unarchiveBlog(@PathVariable Long id, HttpSession session) {
        try {
            if (!sessionService.isAdmin(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Long adminId = sessionService.getCurrentUserId(session);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

            // Chỉ cho phép unarchive nếu status là ARCHIVED
            if (blog.getStatus() != BlogStatus.ARCHIVED) {
                return ResponseEntity.badRequest().body("Chỉ có thể unarchive blog ở trạng thái ARCHIVED");
            }

            blogService.approveBlog(id, adminId); // Unarchive = approve lại
            return ResponseEntity.ok(Map.of("success", true, "message", "Blog unarchived successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Approve delete request - Xóa blog thực sự (PENDING_DELETE -> deleted) - AJAX endpoint
     */
    @PostMapping(value = "/approve-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> approveDelete(@PathVariable Long id, HttpSession session) {
        try {
            if (!sessionService.isAdmin(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Long adminId = sessionService.getCurrentUserId(session);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

            // Chỉ cho phép approve delete nếu status là PENDING_DELETE
            if (blog.getStatus() != BlogStatus.PENDING_DELETE) {
                return ResponseEntity.badRequest().body("Chỉ có thể approve delete blog ở trạng thái PENDING_DELETE");
            }

            blogService.deleteBlog(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Blog đã được xóa thành công"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Reject delete request - Trả về PUBLISHED (PENDING_DELETE -> PUBLISHED) - AJAX endpoint
     */
    @PostMapping(value = "/reject-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> rejectDelete(@PathVariable Long id, HttpSession session) {
        try {
            if (!sessionService.isAdmin(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Long adminId = sessionService.getCurrentUserId(session);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

            // Chỉ cho phép reject delete nếu status là PENDING_DELETE
            if (blog.getStatus() != BlogStatus.PENDING_DELETE) {
                return ResponseEntity.badRequest().body("Chỉ có thể reject delete blog ở trạng thái PENDING_DELETE");
            }

            blogService.approveBlog(id, adminId); // Reject delete = trả về PUBLISHED
            return ResponseEntity.ok(Map.of("success", true, "message", "Yêu cầu xóa đã bị từ chối. Blog đã được trả về trạng thái PUBLISHED"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}


