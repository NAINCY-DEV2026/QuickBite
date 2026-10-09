package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.request.AddressRequest;
import com.quickbite.backend.dto.response.AddressResponse;
import com.quickbite.backend.entity.Address;
import com.quickbite.backend.enums.AddressType;

public class AddressMapper {

    public static AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .houseFlat(a.getHouseFlat())
                .street(a.getStreet())
                .area(a.getArea())
                .city(a.getCity())
                .state(a.getState())
                .pincode(a.getPincode())
                .landmark(a.getLandmark())
                .addressType(a.getAddressType().name())
                .isDefault(a.getIsDefault())
                .build();
    }

    public static Address toEntity(AddressRequest req) {
        return Address.builder()
                .houseFlat(req.getHouseFlat())
                .street(req.getStreet())
                .area(req.getArea())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .landmark(req.getLandmark())
                .addressType(req.getAddressType() != null
                        ? AddressType.valueOf(req.getAddressType())
                        : AddressType.HOME)
                .isDefault(req.getIsDefault() != null ? req.getIsDefault() : false)
                .build();
    }
}