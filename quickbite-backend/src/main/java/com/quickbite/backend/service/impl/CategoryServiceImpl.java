package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.response.CategoryResponse;
import com.quickbite.backend.entity.Category;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.repository.CategoryRepository;
import com.quickbite.backend.repository.FoodItemRepository;
import com.quickbite.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;

    @Override
    @Transactional
    public CategoryResponse create(String name) {
        String clean = name.trim();
        if (categoryRepository.findByName(clean).isPresent()) {
            throw new BadRequestException("Category '" + clean + "' already exists");
        }
        Category saved = categoryRepository.save(Category.builder().name(clean).build());
        return toResponse(saved, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(c -> toResponse(c, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllWithCounts() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(c -> toResponse(c, foodItemRepository.countByCategoryId(c.getId())))
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        long used = foodItemRepository.countByCategoryId(id);
        if (used > 0) {
            throw new BadRequestException("'" + category.getName() + "' is used by " + used
                    + (used == 1 ? " dish" : " dishes") + " and cannot be deleted.");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category c, Long foodCount) {
        return CategoryResponse.builder().id(c.getId()).name(c.getName()).foodCount(foodCount).build();
    }
}