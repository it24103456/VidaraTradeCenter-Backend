package com.vidara.tradecenter.user.mapper;

import com.vidara.tradecenter.user.dto.request.AddressRequest;
import com.vidara.tradecenter.user.dto.request.UpdateProfileRequest;
import com.vidara.tradecenter.user.dto.response.AddressResponse;
import com.vidara.tradecenter.user.dto.response.UserResponse;
import com.vidara.tradecenter.user.model.Address;
import com.vidara.tradecenter.user.model.Role;
import com.vidara.tradecenter.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // Map User entity → UserResponse DTO
    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setProfilePicture(user.getProfilePicture());
        response.setStatus(user.getStatus().name());
        response.setCreatedAt(user.getCreatedAt());

        // Get the first role name (users typically have one primary role)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(Enum::name)
                .orElse("CUSTOMER");
        response.setRole(roleName);

        return response;
    }


    // Map Address entity → AddressResponse DTO
    public AddressResponse toAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setZipCode(address.getZipCode());
        response.setCountry(address.getCountry());
        response.setLabel(address.getLabel());
        response.setRecipientName(address.getRecipientName());
        response.setPhone(address.getPhone());
        response.setDefault(address.isDefault());
        return response;
    }


    // Update User entity from UpdateProfileRequest DTO
    public void updateUserFromRequest(UpdateProfileRequest request, User user) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setProfilePicture(request.getProfilePicture());
    }


    // Map AddressRequest DTO → Address entity
    public Address toAddress(AddressRequest request) {
        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());
        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setDefault(request.isDefault());
        return address;
    }
}
