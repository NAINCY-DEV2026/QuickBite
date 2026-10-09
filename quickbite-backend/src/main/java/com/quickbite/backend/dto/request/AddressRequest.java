package com.quickbite.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    private String houseFlat;
    private String street;
    private String area;
    @NotBlank private String city;
    private String state;
    @NotBlank private String pincode;
    private String landmark;
    private String addressType;
    private Boolean isDefault;
}