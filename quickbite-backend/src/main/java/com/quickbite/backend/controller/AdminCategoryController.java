package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.CategoryRequest;
import com.quickbite.backend.dto.response.CategoryResponse;
import com.quickbite.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAllWithCounts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}