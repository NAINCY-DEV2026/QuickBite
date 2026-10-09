package com.quickbite.backend.controller;

import com.quickbite.backend.dto.request.AddressRequest;
import com.quickbite.backend.dto.response.AddressResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public List<AddressResponse> getMyAddresses() {
        return addressService.getByCustomer(currentUserUtil.getCurrentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse add(@Valid @RequestBody AddressRequest request) {
        return addressService.add(currentUserUtil.getCurrentUserId(), request);
    }

    @PutMapping("/{id}")
    public AddressResponse update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return addressService.update(currentUserUtil.getCurrentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        addressService.delete(currentUserUtil.getCurrentUserId(), id);
    }
}