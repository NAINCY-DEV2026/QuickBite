package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.CouponRequest;
import com.quickbite.backend.dto.response.CouponDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponAdminService {
    Page<CouponDetailResponse> list(Pageable pageable);
    CouponDetailResponse create(CouponRequest request);
    CouponDetailResponse update(Long id, CouponRequest request);
    CouponDetailResponse setActive(Long id, boolean active);

    // true = deleted, false = kept (and disabled) because past orders used it
    boolean delete(Long id);
}