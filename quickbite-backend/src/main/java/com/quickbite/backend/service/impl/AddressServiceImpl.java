package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.request.AddressRequest;
import com.quickbite.backend.dto.response.AddressResponse;
import com.quickbite.backend.entity.Address;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.exception.UnauthorizedException;
import com.quickbite.backend.mapper.AddressMapper;
import com.quickbite.backend.repository.AddressRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressResponse add(Long customerId, AddressRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = AddressMapper.toEntity(request);
        address.setCustomer(customer);
        return AddressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public AddressResponse update(Long customerId, Long addressId, AddressRequest request) {
        Address address = getOwnedOrThrow(customerId, addressId);
        Address updated = AddressMapper.toEntity(request);
        updated.setId(address.getId());
        updated.setCustomer(address.getCustomer());
        return AddressMapper.toResponse(addressRepository.save(updated));
    }

    @Override
    public void delete(Long customerId, Long addressId) {
        Address address = getOwnedOrThrow(customerId, addressId);
        addressRepository.delete(address);
    }

    @Override
    public List<AddressResponse> getByCustomer(Long customerId) {
        return addressRepository.findByCustomerId(customerId).stream()
                .map(AddressMapper::toResponse)
                .toList();
    }

    private Address getOwnedOrThrow(Long customerId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("This address does not belong to you");
        }
        return address;
    }
}