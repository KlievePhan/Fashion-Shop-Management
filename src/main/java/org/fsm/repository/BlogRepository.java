package org.fsm.repository;

import org.fsm.entity.Blog;
import org.fsm.entity.Blog.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    // Tìm blog featured (chỉ PUBLISHED)
    Optional<Blog> findFirstByIsFeaturedTrueAndStatus(BlogStatus status);

    // Lấy tất cả blog PUBLISHED, sắp xếp theo ngày publish
    List<Blog> findByStatusOrderByPublishedAtDesc(BlogStatus status);

    // Lấy blog theo category (chỉ PUBLISHED)
    List<Blog> findByCategoryAndStatusOrderByPublishedAtDesc(String category, BlogStatus status);

    // Tìm kiếm blog theo title hoặc content (chỉ PUBLISHED)
    @Query("SELECT b FROM Blog b WHERE b.status = :status AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Blog> searchPublishedBlogs(@Param("keyword") String keyword, @Param("status") BlogStatus status);

    // Lấy top blog theo view count (chỉ PUBLISHED)
    List<Blog> findTop5ByStatusOrderByViewCountDesc(BlogStatus status);

    // Tăng view count
    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Lấy blog mới nhất (không bao gồm featured, chỉ PUBLISHED)
    @Query("SELECT b FROM Blog b WHERE b.status = :status AND b.isFeatured = false " +
            "ORDER BY b.publishedAt DESC")
    List<Blog> findLatestPublishedBlogs(@Param("status") BlogStatus status);

    // ==================== STAFF MANAGEMENT QUERIES ====================

    // Lấy tất cả blog (bao gồm cả draft, pending, archived) - cho Staff
    List<Blog> findAllByOrderByCreatedAtDesc();

    // Lấy blog theo status - cho Staff
    List<Blog> findByStatusOrderByCreatedAtDesc(BlogStatus status);

    // Lấy blog theo creator - cho Staff
    List<Blog> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    // Phân trang: Lấy blog theo creator với Pageable
    Page<Blog> findByCreatedByOrderByCreatedAtDesc(Long createdBy, Pageable pageable);
    
    // Phân trang: Lấy blog theo status với Pageable
    Page<Blog> findByStatusOrderByCreatedAtDesc(BlogStatus status, Pageable pageable);
    
    // Phân trang: Lấy tất cả blog với Pageable
    Page<Blog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Phân trang: Lấy blog theo creator và status
    Page<Blog> findByCreatedByAndStatusOrderByCreatedAtDesc(Long createdBy, BlogStatus status, Pageable pageable);
    
    // Phân trang: Tìm kiếm blog theo title (cho Staff)
    @Query("SELECT b FROM Blog b WHERE b.createdBy = :createdBy AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY b.createdAt DESC")
    Page<Blog> searchBlogsByCreator(@Param("createdBy") Long createdBy, @Param("keyword") String keyword, Pageable pageable);
    
    // Phân trang: Tìm kiếm blog theo title (cho Admin)
    @Query("SELECT b FROM Blog b WHERE " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY b.createdAt DESC")
    Page<Blog> searchAllBlogs(@Param("keyword") String keyword, Pageable pageable);

    // Lấy blog pending review - cho Manager/Admin
    List<Blog> findByStatusOrderByCreatedAtAsc(BlogStatus status);

    // Tìm blog theo slug
    Optional<Blog> findBySlug(String slug);

    // Đếm số blog theo status
    long countByStatus(BlogStatus status);

    // Đếm số blog của một staff
    long countByCreatedBy(Long createdBy);
}