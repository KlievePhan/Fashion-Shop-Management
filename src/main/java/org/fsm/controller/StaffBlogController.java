package org.fsm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.Blog;
import org.fsm.entity.Blog.BlogStatus;
import org.fsm.entity.User;
import org.fsm.exception.ResourceNotFoundException;
import org.fsm.repository.UserRepository;
import org.fsm.service.BlogService;
import org.fsm.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/staff/blog")
@RequiredArgsConstructor
public class StaffBlogController {

    private final BlogService blogService;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    // ==================== VIEW METHODS ====================

    /**
     * Hiển thị danh sách blog của staff - redirect về staff panel với tab blogs
     */
    @GetMapping
    public String listMyBlogs(
            @RequestParam(required = false) String status,
            HttpSession session) {
        // Redirect về staff panel với tab blogs active
        if (status != null && !status.isEmpty()) {
            return "redirect:/staff?tab=blogs&status=" + status;
        }
        return "redirect:/staff?tab=blogs";
    }

    // ==================== VIEW METHODS ====================

    /**
     * Hiển thị chi tiết blog (cho Staff xem) - Phải đặt trước route /{id} để tránh conflict
     */
    @GetMapping("/view/{id}")
    public String viewBlog(@PathVariable Long id, Model model, HttpSession session) {
        Long staffId = sessionService.getCurrentUserId(session);
        if (staffId == null) {
            return "redirect:/login?error=session_expired";
        }

        Blog blog = blogService.getBlogByIdForStaff(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

        // Kiểm tra quyền - staff chỉ có thể xem blog của chính mình
        if (!blog.getCreatedBy().equals(staffId)) {
            return "redirect:/staff?tab=blogs&error=unauthorized";
        }

        model.addAttribute("blog", blog);
        model.addAttribute("userRole", "staff");
        
        // Tạo backUrl với status filter nếu có
        String backUrl = "/staff?tab=blogs";
        if (blog.getStatus() != null) {
            backUrl += "&status=" + blog.getStatus().name();
        }
        model.addAttribute("backUrl", backUrl);
        
        return "blog-detail-management";
    }

    // ==================== API METHODS (JSON) ====================

    /**
     * Get blog by ID (JSON) - để load vào modal edit
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getBlog(@PathVariable Long id, HttpSession session) {
        try {
            Long staffId = sessionService.getCurrentUserId(session);
            if (staffId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + id));

            // Kiểm tra quyền
            if (!blog.getCreatedBy().equals(staffId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", blog.getId());
            response.put("title", blog.getTitle());
            response.put("excerpt", blog.getExcerpt());
            response.put("content", blog.getContent());
            response.put("category", blog.getCategory());
            response.put("readTime", blog.getReadTime());
            response.put("imageUrl", blog.getImageUrl());
            response.put("author", blog.getAuthor());
            response.put("tags", blog.getTags());
            response.put("metaDescription", blog.getMetaDescription());
            response.put("slug", blog.getSlug());
            response.put("isFeatured", blog.getIsFeatured());
            response.put("status", blog.getStatus().name());

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading blog");
        }
    }

    // DTO để nhận dữ liệu JSON từ form
    @Data
    public static class BlogDto {
        private Long id;
        private String title;
        private String excerpt;
        private String content;
        private String category;
        private Integer readTime;
        private String imageUrl;
        private String author;
        private String tags;
        private String metaDescription;
        private String slug;
        private Boolean isFeatured;
    }

    /**
     * Save blog (create or update) - AJAX endpoint
     */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveBlog(@RequestBody BlogDto blogDto, HttpSession session) {
        try {
            Long staffId = sessionService.getCurrentUserId(session);
            if (staffId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            User currentUser = userRepository.findById(staffId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Blog blog;
            boolean isNew = (blogDto.getId() == null);

            if (!isNew) {
                // UPDATE
                blog = blogService.getBlogByIdForStaff(blogDto.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

                // Kiểm tra quyền
                if (!blog.getCreatedBy().equals(staffId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
                }

                // Lưu lại status hiện tại để xử lý logic
                BlogStatus currentStatus = blog.getStatus();

                // Create Blog object with updated fields for updateBlog method
                Blog updatedBlog = new Blog();
                updatedBlog.setTitle(blogDto.getTitle());
                updatedBlog.setExcerpt(blogDto.getExcerpt());
                updatedBlog.setContent(blogDto.getContent());
                updatedBlog.setCategory(blogDto.getCategory());
                updatedBlog.setReadTime(blogDto.getReadTime());
                updatedBlog.setImageUrl(blogDto.getImageUrl());
                updatedBlog.setAuthor(blogDto.getAuthor() != null && !blogDto.getAuthor().isEmpty() 
                        ? blogDto.getAuthor() : currentUser.getFullName());
                updatedBlog.setTags(blogDto.getTags());
                updatedBlog.setMetaDescription(blogDto.getMetaDescription());
                updatedBlog.setSlug(blogDto.getSlug() != null && !blogDto.getSlug().isEmpty() 
                        ? blogDto.getSlug() : null);
                updatedBlog.setIsFeatured(blogDto.getIsFeatured() != null ? blogDto.getIsFeatured() : false);

                // Nếu blog hiện tại là PUBLISHED, sau khi update sẽ chuyển về PENDING_REVIEW để admin duyệt lại
                // Nếu là DRAFT hoặc PENDING_REVIEW, giữ nguyên status
                Map<String, Object> response = new HashMap<>();
                if (currentStatus == BlogStatus.PUBLISHED) {
                    blogService.updateBlogAndRequestReview(blogDto.getId(), updatedBlog, staffId);
                    response.put("success", true);
                    response.put("message", "Blog đã được cập nhật. Bài viết đã được chuyển về trạng thái PENDING_REVIEW để admin duyệt lại.");
                } else {
                    blogService.updateBlog(blogDto.getId(), updatedBlog, staffId);
                    response.put("success", true);
                    response.put("message", "Blog đã được cập nhật thành công.");
                }
                return ResponseEntity.ok(response);
            } else {
                // CREATE
                blog = new Blog();
                blog.setTitle(blogDto.getTitle());
                blog.setExcerpt(blogDto.getExcerpt());
                blog.setContent(blogDto.getContent());
                blog.setCategory(blogDto.getCategory());
                blog.setReadTime(blogDto.getReadTime());
                blog.setImageUrl(blogDto.getImageUrl());
                blog.setAuthor(blogDto.getAuthor() != null && !blogDto.getAuthor().isEmpty() 
                        ? blogDto.getAuthor() : currentUser.getFullName());
                blog.setTags(blogDto.getTags());
                blog.setMetaDescription(blogDto.getMetaDescription());
                // Generate slug if not provided
                if (blogDto.getSlug() == null || blogDto.getSlug().isEmpty()) {
                    blog.setSlug(null); // Let BlogService generate it
                } else {
                    blog.setSlug(blogDto.getSlug());
                }
                blog.setIsFeatured(blogDto.getIsFeatured() != null ? blogDto.getIsFeatured() : false);
                blog.setStatus(BlogStatus.DRAFT);

                blogService.createBlog(blog, staffId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Blog đã được tạo thành công.");
                return ResponseEntity.ok(response);
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Submit blog để review (DRAFT -> PENDING_REVIEW) - AJAX
     */
    @PostMapping("/submit/{id}")
    @ResponseBody
    public ResponseEntity<?> submitForReview(@PathVariable Long id, HttpSession session) {
        try {
            Long staffId = sessionService.getCurrentUserId(session);
            if (staffId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

            // Kiểm tra quyền
            if (!blog.getCreatedBy().equals(staffId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            // Chỉ cho phép submit nếu status là DRAFT
            if (blog.getStatus() != BlogStatus.DRAFT) {
                return ResponseEntity.badRequest().body("Chỉ có thể submit blog ở trạng thái DRAFT");
            }

            blogService.submitForReview(id, staffId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Blog submitted for review successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Yêu cầu xóa blog PUBLISHED - chuyển sang PENDING_DELETE để admin duyệt - AJAX
     */
    @PostMapping("/request-delete/{id}")
    @ResponseBody
    public ResponseEntity<?> requestDeleteBlog(@PathVariable Long id, HttpSession session) {
        try {
            Long staffId = sessionService.getCurrentUserId(session);
            if (staffId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

            // Kiểm tra quyền
            if (!blog.getCreatedBy().equals(staffId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            // Chỉ cho phép request delete nếu status là PUBLISHED
            if (blog.getStatus() != BlogStatus.PUBLISHED) {
                return ResponseEntity.badRequest().body("Chỉ có thể yêu cầu xóa blog ở trạng thái PUBLISHED");
            }

            blogService.requestDelete(id, staffId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Yêu cầu xóa blog đã được gửi đến admin. Blog đã được chuyển về trạng thái PENDING_DELETE.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Xóa blog trực tiếp - chỉ cho DRAFT và PENDING_REVIEW - AJAX
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBlog(@PathVariable Long id, HttpSession session) {
        try {
            Long staffId = sessionService.getCurrentUserId(session);
            if (staffId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
            }

            Blog blog = blogService.getBlogByIdForStaff(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

            // Kiểm tra quyền
            if (!blog.getCreatedBy().equals(staffId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            // Chỉ cho phép xóa trực tiếp nếu status là DRAFT hoặc PENDING_REVIEW
            if (blog.getStatus() != BlogStatus.DRAFT && blog.getStatus() != BlogStatus.PENDING_REVIEW) {
                return ResponseEntity.badRequest().body("Chỉ có thể xóa trực tiếp blog ở trạng thái DRAFT hoặc PENDING_REVIEW");
            }

            blogService.deleteBlog(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Blog đã được xóa thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
