package com.serviceconnect.user.service;

import com.serviceconnect.user.dto.request.UserRequest;
import com.serviceconnect.user.dto.response.UserResponse;
import com.serviceconnect.user.entity.User;
import com.serviceconnect.user.exception.UserNotFoundException;
import com.serviceconnect.user.exception.UserProfileAlreadyExistsException;
import com.serviceconnect.user.mapper.UserMapper;
import com.serviceconnect.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse createUser(
            Long userId,
            UserRequest request) {

        if (userRepository.existsById(userId)) {
            throw new UserProfileAlreadyExistsException(
                    "User profile already exists"
            );
        }

        User user = userMapper.toEntity(userId, request);

        LocalDateTime now = LocalDateTime.now();

        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse updateUser(
            Long authenticatedUserId,
            Long id,
            UserRequest request) {

        if (!authenticatedUserId.equals(id)) {
            throw new AccessDeniedException(
                    "You are not allowed to update this user"
            );
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        userMapper.updateEntity(existingUser, request);

        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);

        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(
            Long authenticatedUserId,
            Long id) {

        if (!authenticatedUserId.equals(id)) {
            throw new AccessDeniedException(
                    "You are not allowed to delete this user"
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        userRepository.delete(user);
    }
}