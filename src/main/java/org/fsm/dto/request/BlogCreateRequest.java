package org.fsm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlogCreateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Excerpt is required")
    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    private String excerpt;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    private Integer readTime;

    private Boolean isFeatured = false;

    @Size(max = 200, message = "Author name must not exceed 200 characters")
    private String author;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @Size(max = 160, message = "Meta description must not exceed 160 characters")
    private String metaDescription;

    @Size(max = 200, message = "Slug must not exceed 200 characters")
    private String slug;
}


