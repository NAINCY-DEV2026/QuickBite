package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull private Long orderId;
    @NotNull @Min(1) @Max(5) private Integer rating;
    private String comment;
}