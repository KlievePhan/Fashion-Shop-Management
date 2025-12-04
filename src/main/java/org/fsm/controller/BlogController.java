package org.fsm.controller;

import org.fsm.entity.Blog;
import org.fsm.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    // Hiển thị trang danh sách blog
    @GetMapping
    public String blogPage(Model model) {
        // Lấy featured blog
        blogService.getFeaturedBlog().ifPresent(blog -> model.addAttribute("featuredBlog", blog));

        // Lấy tất cả blog mới nhất
        List<Blog> blogs = blogService.getLatestBlogs();
        model.addAttribute("blogs", blogs);

        // Lấy top blog phổ biến
        List<Blog> popularBlogs = blogService.getPopularBlogs();
        model.addAttribute("popularBlogs", popularBlogs);

        return "blog";
    }

    // Hiển thị chi tiết một blog
    @GetMapping("/{id}")
    public String blogDetail(@PathVariable Long id, Model model) {
        Blog blog = blogService.getBlogByIdAndIncrementView(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        model.addAttribute("blog", blog);

        // Lấy các blog liên quan (cùng category)
        List<Blog> relatedBlogs = blogService.getBlogsByCategory(blog.getCategory())
                .stream()
                .filter(b -> !b.getId().equals(id))
                .limit(3)
                .toList();
        model.addAttribute("relatedBlogs", relatedBlogs);

        return "blog-detail";
    }

    // Lọc blog theo category
    @GetMapping("/category/{category}")
    public String blogsByCategory(@PathVariable String category, Model model) {
        // Lấy featured blog
        blogService.getFeaturedBlog().ifPresent(blog -> model.addAttribute("featuredBlog", blog));

        List<Blog> blogs = blogService.getBlogsByCategory(category);
        model.addAttribute("blogs", blogs);
        model.addAttribute("category", category);

        // Lấy top blog phổ biến
        List<Blog> popularBlogs = blogService.getPopularBlogs();
        model.addAttribute("popularBlogs", popularBlogs);

        return "blog";
    }

    // Tìm kiếm blog
    @GetMapping("/search")
    public String searchBlogs(@RequestParam String keyword, Model model) {
        // Lấy featured blog
        blogService.getFeaturedBlog().ifPresent(blog -> model.addAttribute("featuredBlog", blog));

        List<Blog> blogs = blogService.searchBlogs(keyword);
        model.addAttribute("blogs", blogs);
        model.addAttribute("keyword", keyword);

        // Lấy top blog phổ biến
        List<Blog> popularBlogs = blogService.getPopularBlogs();
        model.addAttribute("popularBlogs", popularBlogs);

        return "blog";
    }
}