package com.quickbite.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String houseFlat;
    private String street;
    private String area;
    private String city;
    private String state;
    private String pincode;
    private String landmark;
    private String addressType;
    private Boolean isDefault;
}