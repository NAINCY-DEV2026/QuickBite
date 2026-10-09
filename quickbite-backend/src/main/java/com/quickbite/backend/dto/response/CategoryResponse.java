package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Long foodCount;   // only filled for the admin list
}