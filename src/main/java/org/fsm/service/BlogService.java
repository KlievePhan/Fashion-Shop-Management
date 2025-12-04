package org.fsm.service;

import org.fsm.entity.Blog;
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

    // Lấy tất cả blog đã publish
    public List<Blog> getAllPublishedBlogs() {
        return blogRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    // Lấy blog featured
    public Optional<Blog> getFeaturedBlog() {
        return blogRepository.findFirstByIsFeaturedTrueAndPublishedTrue();
    }

    // Lấy blog theo ID
    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    // Lấy blog theo ID và tăng view count
    @Transactional
    public Optional<Blog> getBlogByIdAndIncrementView(Long id) {
        Optional<Blog> blog = blogRepository.findById(id);
        if (blog.isPresent() && blog.get().getPublished()) {
            blogRepository.incrementViewCount(id);
            // Reload blog để lấy view count mới
            return blogRepository.findById(id);
        }
        return Optional.empty();
    }

    // Lấy blog theo category
    public List<Blog> getBlogsByCategory(String category) {
        return blogRepository.findByCategoryAndPublishedTrueOrderByCreatedAtDesc(category);
    }

    // Tìm kiếm blog
    public List<Blog> searchBlogs(String keyword) {
        return blogRepository.searchBlogs(keyword);
    }

    // Lấy top blog phổ biến
    public List<Blog> getPopularBlogs() {
        return blogRepository.findTop5ByPublishedTrueOrderByViewCountDesc();
    }

    // Lấy blog mới nhất (không bao gồm featured)
    public List<Blog> getLatestBlogs() {
        return blogRepository.findLatestBlogs();
    }

    // Tạo blog mới
    @Transactional
    public Blog createBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    // Cập nhật blog
    @Transactional
    public Blog updateBlog(Long id, Blog blogDetails) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));

        blog.setTitle(blogDetails.getTitle());
        blog.setExcerpt(blogDetails.getExcerpt());
        blog.setContent(blogDetails.getContent());
        blog.setCategory(blogDetails.getCategory());
        blog.setImageUrl(blogDetails.getImageUrl());
        blog.setReadTime(blogDetails.getReadTime());
        blog.setIsFeatured(blogDetails.getIsFeatured());
        blog.setPublished(blogDetails.getPublished());
        blog.setAuthor(blogDetails.getAuthor());

        return blogRepository.save(blog);
    }

    // Xóa blog
    @Transactional
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
}