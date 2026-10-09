package com.quickbite.backend.service;

import com.quickbite.backend.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(String name);
    List<CategoryResponse> getAll();
    List<CategoryResponse> getAllWithCounts();
    void delete(Long id);
}