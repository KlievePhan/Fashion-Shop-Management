package org.fsm.service;

import org.fsm.entity.Blog;
import org.fsm.entity.Blog.BlogStatus;
import org.fsm.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    // ==================== PUBLIC/CUSTOMER METHODS ====================

    // Lấy tất cả blog đã publish
    public List<Blog> getAllPublishedBlogs() {
        return blogRepository.findByStatusOrderByPublishedAtDesc(BlogStatus.PUBLISHED);
    }

    // Lấy blog featured
    public Optional<Blog> getFeaturedBlog() {
        return blogRepository.findFirstByIsFeaturedTrueAndStatus(BlogStatus.PUBLISHED);
    }

    // Lấy blog theo ID (chỉ PUBLISHED)
    public Optional<Blog> getPublishedBlogById(Long id) {
        return blogRepository.findById(id)
                .filter(blog -> blog.getStatus() == BlogStatus.PUBLISHED);
    }

    // Lấy blog theo ID và tăng view count
    @Transactional
    public Optional<Blog> getBlogByIdAndIncrementView(Long id) {
        Optional<Blog> blog = blogRepository.findById(id);
        if (blog.isPresent() && blog.get().getStatus() == BlogStatus.PUBLISHED) {
            blogRepository.incrementViewCount(id);
            return blogRepository.findById(id);
        }
        return Optional.empty();
    }

    // Lấy blog theo category
    public List<Blog> getBlogsByCategory(String category) {
        return blogRepository.findByCategoryAndStatusOrderByPublishedAtDesc(category, BlogStatus.PUBLISHED);
    }

    // Tìm kiếm blog
    public List<Blog> searchBlogs(String keyword) {
        return blogRepository.searchPublishedBlogs(keyword, BlogStatus.PUBLISHED);
    }

    // Lấy top blog phổ biến
    public List<Blog> getPopularBlogs() {
        return blogRepository.findTop5ByStatusOrderByViewCountDesc(BlogStatus.PUBLISHED);
    }

    // Lấy blog mới nhất (không bao gồm featured)
    public List<Blog> getLatestBlogs() {
        return blogRepository.findLatestPublishedBlogs(BlogStatus.PUBLISHED);
    }

    // Lấy blog theo slug
    public Optional<Blog> getBlogBySlug(String slug) {
        return blogRepository.findBySlug(slug)
                .filter(blog -> blog.getStatus() == BlogStatus.PUBLISHED);
    }

    // Lấy blog theo slug và tăng view count
    @Transactional
    public Optional<Blog> getBlogBySlugAndIncrementView(String slug) {
        Optional<Blog> blog = blogRepository.findBySlug(slug);
        if (blog.isPresent() && blog.get().getStatus() == BlogStatus.PUBLISHED) {
            blogRepository.incrementViewCount(blog.get().getId());
            return blogRepository.findBySlug(slug);
        }
        return Optional.empty();
    }

    // Tạo slug từ title
    public String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        
        // Normalize Vietnamese characters
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutDiacritics = pattern.matcher(normalized).replaceAll("");
        
        // Convert to lowercase and replace spaces with hyphens
        String slug = withoutDiacritics
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
        
        // Ensure slug is not empty
        if (slug.isEmpty()) {
            slug = "blog-" + System.currentTimeMillis();
        }
        
        // Check if slug already exists, if yes, append number
        String finalSlug = slug;
        int counter = 1;
        while (blogRepository.findBySlug(finalSlug).isPresent()) {
            finalSlug = slug + "-" + counter;
            counter++;
        }
        
        return finalSlug;
    }

    // ==================== STAFF MANAGEMENT METHODS ====================

    // Lấy tất cả blog (cho Staff) - bao gồm draft, pending, archived
    public List<Blog> getAllBlogsForStaff() {
        return blogRepository.findAllByOrderByCreatedAtDesc();
    }

    // Lấy blog theo status (cho Staff)
    public List<Blog> getBlogsByStatus(BlogStatus status) {
        return blogRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    // Lấy blog của một staff
    public List<Blog> getBlogsByCreator(Long staffId) {
        return blogRepository.findByCreatedByOrderByCreatedAtDesc(staffId);
    }

    // Lấy blog pending review (cho Manager)
    public List<Blog> getPendingBlogs() {
        return blogRepository.findByStatusOrderByCreatedAtAsc(BlogStatus.PENDING_REVIEW);
    }

    // Lấy bất kỳ blog nào theo ID (cho Staff)
    public Optional<Blog> getBlogByIdForStaff(Long id) {
        return blogRepository.findById(id);
    }

    // Tạo blog mới (Staff)
    @Transactional
    public Blog createBlog(Blog blog, Long staffId) {
        blog.setCreatedBy(staffId);
        blog.setUpdatedBy(staffId);
        if (blog.getStatus() == null) {
            blog.setStatus(BlogStatus.DRAFT);
        }
        // Generate slug if not provided
        if (blog.getSlug() == null || blog.getSlug().isEmpty()) {
            blog.setSlug(generateSlug(blog.getTitle()));
        }
        return blogRepository.save(blog);
    }

    // Cập nhật blog (Staff)
    @Transactional
    public Blog updateBlog(Long id, Blog blogDetails, Long staffId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));

        blog.setTitle(blogDetails.getTitle());
        blog.setExcerpt(blogDetails.getExcerpt());
        blog.setContent(blogDetails.getContent());
        blog.setCategory(blogDetails.getCategory());
        blog.setImageUrl(blogDetails.getImageUrl());
        blog.setReadTime(blogDetails.getReadTime());
        blog.setIsFeatured(blogDetails.getIsFeatured());
        blog.setAuthor(blogDetails.getAuthor());
        blog.setTags(blogDetails.getTags());
        blog.setMetaDescription(blogDetails.getMetaDescription());
        
        // Update slug if title changed or slug is empty
        if (blogDetails.getSlug() != null && !blogDetails.getSlug().isEmpty()) {
            blog.setSlug(blogDetails.getSlug());
        } else if (!blog.getTitle().equals(blogDetails.getTitle())) {
            blog.setSlug(generateSlug(blogDetails.getTitle()));
        }
        
        blog.setUpdatedBy(staffId);

        return blogRepository.save(blog);
    }

    // Cập nhật blog và tự động chuyển về PENDING_REVIEW nếu đang PUBLISHED (Staff edit published blog)
    @Transactional
    public Blog updateBlogAndRequestReview(Long id, Blog blogDetails, Long staffId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));

        blog.setTitle(blogDetails.getTitle());
        blog.setExcerpt(blogDetails.getExcerpt());
        blog.setContent(blogDetails.getContent());
        blog.setCategory(blogDetails.getCategory());
        blog.setImageUrl(blogDetails.getImageUrl());
        blog.setReadTime(blogDetails.getReadTime());
        blog.setIsFeatured(blogDetails.getIsFeatured());
        blog.setAuthor(blogDetails.getAuthor());
        blog.setTags(blogDetails.getTags());
        blog.setMetaDescription(blogDetails.getMetaDescription());
        
        // Update slug if title changed or slug is empty
        if (blogDetails.getSlug() != null && !blogDetails.getSlug().isEmpty()) {
            blog.setSlug(blogDetails.getSlug());
        } else if (!blog.getTitle().equals(blogDetails.getTitle())) {
            blog.setSlug(generateSlug(blogDetails.getTitle()));
        }
        
        // Chuyển status về PENDING_REVIEW để admin duyệt lại
        blog.setStatus(BlogStatus.PENDING_REVIEW);
        blog.setUpdatedBy(staffId);

        return blogRepository.save(blog);
    }

    // Thay đổi status của blog
    @Transactional
    public Blog changeStatus(Long id, BlogStatus newStatus, Long staffId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));

        blog.setStatus(newStatus);
        blog.setUpdatedBy(staffId);

        return blogRepository.save(blog);
    }

    // Submit blog để review (Staff gửi để Manager duyệt)
    @Transactional
    public Blog submitForReview(Long id, Long staffId) {
        return changeStatus(id, BlogStatus.PENDING_REVIEW, staffId);
    }

    // Approve blog (Manager duyệt và publish)
    @Transactional
    public Blog approveBlog(Long id, Long managerId) {
        return changeStatus(id, BlogStatus.PUBLISHED, managerId);
    }

    // Reject blog (Manager từ chối, trả về Draft)
    @Transactional
    public Blog rejectBlog(Long id, Long managerId) {
        return changeStatus(id, BlogStatus.DRAFT, managerId);
    }

    // Archive blog (ẩn blog khỏi website)
    @Transactional
    public Blog archiveBlog(Long id, Long staffId) {
        return changeStatus(id, BlogStatus.ARCHIVED, staffId);
    }

    // Request delete blog (Staff gửi yêu cầu xóa PUBLISHED blog)
    @Transactional
    public Blog requestDelete(Long id, Long staffId) {
        return changeStatus(id, BlogStatus.PENDING_DELETE, staffId);
    }

    // Xóa blog (thực sự xóa khỏi database)
    @Transactional
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }

    // Đếm số blog theo status
    public long countBlogsByStatus(BlogStatus status) {
        return blogRepository.countByStatus(status);
    }

    // Đếm số blog của một staff
    public long countBlogsByCreator(Long staffId) {
        return blogRepository.countByCreatedBy(staffId);
    }

    // Đếm tổng số blog (cho Admin)
    public long countAllBlogs() {
        return blogRepository.count();
    }

    // ==================== PAGINATION METHODS ====================

    // Phân trang: Lấy blog của một staff với Pageable
    public Page<Blog> getBlogsByCreator(Long staffId, Pageable pageable) {
        return blogRepository.findByCreatedByOrderByCreatedAtDesc(staffId, pageable);
    }

    // Phân trang: Lấy blog của staff theo status
    public Page<Blog> getBlogsByCreatorAndStatus(Long staffId, BlogStatus status, Pageable pageable) {
        return blogRepository.findByCreatedByAndStatusOrderByCreatedAtDesc(staffId, status, pageable);
    }

    // Phân trang: Lấy tất cả blog cho admin với Pageable
    public Page<Blog> getAllBlogsForStaff(Pageable pageable) {
        return blogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // Phân trang: Lấy blog theo status cho admin
    public Page<Blog> getBlogsByStatus(BlogStatus status, Pageable pageable) {
        return blogRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    // Phân trang: Tìm kiếm blog của staff
    public Page<Blog> searchBlogsByCreator(Long staffId, String keyword, Pageable pageable) {
        return blogRepository.searchBlogsByCreator(staffId, keyword, pageable);
    }

    // Phân trang: Tìm kiếm blog (cho admin)
    public Page<Blog> searchAllBlogs(String keyword, Pageable pageable) {
        return blogRepository.searchAllBlogs(keyword, pageable);
    }
}