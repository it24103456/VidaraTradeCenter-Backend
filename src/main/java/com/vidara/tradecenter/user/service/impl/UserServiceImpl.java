package com.vidara.tradecenter.user.service.impl;

import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.user.dto.request.UpdateProfileRequest;
import com.vidara.tradecenter.user.dto.response.UserResponse;
import com.vidara.tradecenter.user.mapper.UserMapper;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.model.enums.UserStatus;
import com.vidara.tradecenter.user.repository.UserRepository;
import com.vidara.tradecenter.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    // GET PROFILE

    @Override
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return userMapper.toUserResponse(user);
    }


    // UPDATE PROFILE

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update fields from request
        userMapper.updateUserFromRequest(request, user);

        // Save updated user
        User updatedUser = userRepository.save(user);
        logger.info("User profile updated: {}", updatedUser.getEmail());

        return userMapper.toUserResponse(updatedUser);
    }


    // GET ALL USERS (ADMIN)

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }


    // UPDATE USER STATUS (ADMIN)

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(status);
        userRepository.save(user);
        logger.info("User status updated: {} → {}", user.getEmail(), status);
    }
}
