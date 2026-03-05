package com.vidara.tradecenter.user.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import com.vidara.tradecenter.user.dto.request.AddressRequest;
import com.vidara.tradecenter.user.dto.response.AddressResponse;
import com.vidara.tradecenter.user.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }


    // ADD ADDRESS
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse addressResponse = addressService.addAddress(currentUser.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", addressResponse));
    }


    // GET ALL ADDRESSES
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @CurrentUser CustomUserDetails currentUser) {
        List<AddressResponse> addresses = addressService.getAddresses(currentUser.getId());
        return ResponseEntity
                .ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }


    // UPDATE ADDRESS
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse addressResponse = addressService.updateAddress(currentUser.getId(), id, request);
        return ResponseEntity
                .ok(ApiResponse.success("Address updated successfully", addressResponse));
    }


    // DELETE ADDRESS
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        addressService.deleteAddress(currentUser.getId(), id);
        return ResponseEntity
                .ok(ApiResponse.success("Address deleted successfully"));
    }


    // SET DEFAULT ADDRESS
    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        addressService.setDefaultAddress(currentUser.getId(), id);
        return ResponseEntity
                .ok(ApiResponse.success("Default address updated successfully"));
    }
}
