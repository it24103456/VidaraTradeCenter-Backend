package com.vidara.tradecenter.user.service;

import com.vidara.tradecenter.user.dto.request.AddressRequest;
import com.vidara.tradecenter.user.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    // Add a new address for a user
    AddressResponse addAddress(Long userId, AddressRequest request);

    // Get all addresses for a user
    List<AddressResponse> getAddresses(Long userId);

    // Update an existing address
    AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);

    // Delete an address
    void deleteAddress(Long userId, Long addressId);

    // Set an address as default
    void setDefaultAddress(Long userId, Long addressId);
}
