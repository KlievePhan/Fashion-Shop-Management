package org.fsm.repository;

import org.fsm.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    // Tìm blog featured
    Optional<Blog> findFirstByIsFeaturedTrueAndPublishedTrue();

    // Lấy tất cả blog đã publish, sắp xếp theo ngày tạo
    List<Blog> findByPublishedTrueOrderByCreatedAtDesc();

    // Lấy blog theo category
    List<Blog> findByCategoryAndPublishedTrueOrderByCreatedAtDesc(String category);

    // Tìm kiếm blog theo title hoặc content
    @Query("SELECT b FROM Blog b WHERE b.published = true AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Blog> searchBlogs(@Param("keyword") String keyword);

    // Lấy top blog theo view count
    List<Blog> findTop5ByPublishedTrueOrderByViewCountDesc();

    // Tăng view count
    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Lấy blog mới nhất (không bao gồm featured)
    @Query("SELECT b FROM Blog b WHERE b.published = true AND b.isFeatured = false " +
            "ORDER BY b.createdAt DESC")
    List<Blog> findLatestBlogs();
}