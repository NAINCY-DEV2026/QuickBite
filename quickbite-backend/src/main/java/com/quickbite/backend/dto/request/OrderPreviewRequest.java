package com.quickbite.backend.dto.request;

import lombok.Data;

@Data
public class OrderPreviewRequest {
    private String couponCode; // optional
}