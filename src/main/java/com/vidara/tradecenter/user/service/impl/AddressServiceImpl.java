package com.vidara.tradecenter.user.service.impl;

import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.user.dto.request.AddressRequest;
import com.vidara.tradecenter.user.dto.response.AddressResponse;
import com.vidara.tradecenter.user.mapper.UserMapper;
import com.vidara.tradecenter.user.model.Address;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.AddressRepository;
import com.vidara.tradecenter.user.repository.UserRepository;
import com.vidara.tradecenter.user.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AddressServiceImpl(AddressRepository addressRepository,
                              UserRepository userRepository,
                              UserMapper userMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    // ADD ADDRESS

    @Override
    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = userMapper.toAddress(request);
        address.setUser(user);

        // If this is the first address or marked as default, handle defaults
        if (request.isDefault() || addressRepository.countByUserId(userId) == 0) {
            addressRepository.unsetAllDefaultsForUser(userId);
            address.setDefault(true);
        }

        Address savedAddress = addressRepository.save(address);
        logger.info("Address added for user {}: {}", user.getEmail(), savedAddress.getId());

        return userMapper.toAddressResponse(savedAddress);
    }


    // GET ADDRESSES

    @Override
    public List<AddressResponse> getAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return addressRepository.findByUserId(userId).stream()
                .map(userMapper::toAddressResponse)
                .collect(Collectors.toList());
    }


    // UPDATE ADDRESS

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Update fields
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());
        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());

        // Handle default flag
        if (request.isDefault() && !address.isDefault()) {
            addressRepository.unsetAllDefaultsForUser(userId);
            address.setDefault(true);
        } else if (!request.isDefault()) {
            address.setDefault(false);
        }

        Address updatedAddress = addressRepository.save(address);
        logger.info("Address updated: {}", updatedAddress.getId());

        return userMapper.toAddressResponse(updatedAddress);
    }


    // DELETE ADDRESS

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        addressRepository.delete(address);
        logger.info("Address deleted: {}", addressId);
    }


    // SET DEFAULT ADDRESS

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Unset all existing defaults for this user
        addressRepository.unsetAllDefaultsForUser(userId);

        // Set the new default
        address.setDefault(true);
        addressRepository.save(address);
        logger.info("Default address set: {} for user: {}", addressId, userId);
    }
}
