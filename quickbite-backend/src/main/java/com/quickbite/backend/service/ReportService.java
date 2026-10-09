package com.quickbite.backend.service;

import com.quickbite.backend.dto.response.ReportResponse;

public interface ReportService {
    ReportResponse getReport(int days);
}