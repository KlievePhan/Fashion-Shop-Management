package org.fsm.service;

import org.fsm.entity.Blog;
import org.fsm.entity.Blog.BlogStatus;
import org.fsm.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

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
        blog.setSlug(blogDetails.getSlug());
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

    // Xóa blog
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
}