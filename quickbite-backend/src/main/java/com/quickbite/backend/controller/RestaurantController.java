package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.RestaurantRequest;
import com.quickbite.backend.dto.response.RestaurantResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final CurrentUserUtil currentUserUtil;

    // GET /api/restaurants?city=&name=&cuisine=&minRating=4&openNow=true&page=0&size=6&sort=rating,desc
    @GetMapping
    public Page<RestaurantResponse> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "false") boolean openNow,
            Pageable pageable) {
        return restaurantService.search(city, name, cuisine, minRating, openNow, pageable);
    }

    // The logged-in owner's own restaurants
    @GetMapping("/mine")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public List<RestaurantResponse> getMine() {
        return restaurantService.getByOwner(currentUserUtil.getCurrentUserId());
    }

    @GetMapping("/{id}")
    public RestaurantResponse getById(@PathVariable Long id) {
        return restaurantService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public RestaurantResponse create(@Valid @RequestBody RestaurantRequest request) {
        return restaurantService.create(currentUserUtil.getCurrentUserId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public RestaurantResponse update(@PathVariable Long id, @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.update(currentUserUtil.getCurrentUserId(), id, request);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public RestaurantResponse uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return restaurantService.updateImage(currentUserUtil.getCurrentUserId(), id, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public void delete(@PathVariable Long id) {
        restaurantService.delete(currentUserUtil.getCurrentUserId(), id);
    }
}