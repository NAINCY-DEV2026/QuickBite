package com.quickbite.backend.service;

import com.quickbite.backend.dto.request.AddressRequest;
import com.quickbite.backend.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {
    AddressResponse add(Long customerId, AddressRequest request);
    AddressResponse update(Long customerId, Long addressId, AddressRequest request);
    void delete(Long customerId, Long addressId);
    List<AddressResponse> getByCustomer(Long customerId);
}