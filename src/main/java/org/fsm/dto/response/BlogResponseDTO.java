package org.fsm.dto.response;

import lombok.*;
import org.fsm.entity.Blog;
import org.fsm.entity.Blog.BlogStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogResponseDTO {
    private Long id;
    private String title;
    private String excerpt;
    private String content;
    private String category;
    private String imageUrl;
    private Integer readTime;
    private Boolean isFeatured;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private BlogStatus status;
    private String author;
    private Long createdBy;
    private Long updatedBy;
    private String tags;
    private String metaDescription;
    private String slug;

    // Static method to convert Entity to DTO
    public static BlogResponseDTO fromEntity(Blog blog) {
        return BlogResponseDTO.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .excerpt(blog.getExcerpt())
                .content(blog.getContent())
                .category(blog.getCategory())
                .imageUrl(blog.getImageUrl())
                .readTime(blog.getReadTime())
                .isFeatured(blog.getIsFeatured())
                .viewCount(blog.getViewCount())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .publishedAt(blog.getPublishedAt())
                .status(blog.getStatus())
                .author(blog.getAuthor())
                .createdBy(blog.getCreatedBy())
                .updatedBy(blog.getUpdatedBy())
                .tags(blog.getTags())
                .metaDescription(blog.getMetaDescription())
                .slug(blog.getSlug())
                .build();
    }
}


