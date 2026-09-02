package com.serviceconnect.user.service;

import com.serviceconnect.user.dto.request.AccountSettingsRequest;
import com.serviceconnect.user.dto.request.UserRequest;
import com.serviceconnect.user.dto.response.AccountSettingsResponse;
import com.serviceconnect.user.dto.response.OnboardingStatusResponse;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;


    // ============================================================
    // CREATE USER PROFILE
    // ============================================================

    public UserResponse createUser(
            Long userId,
            UserRequest request) {

        if (userRepository.existsById(userId)) {

            throw new UserProfileAlreadyExistsException(
                    "User profile already exists"
            );
        }

        User user =
                userMapper.toEntity(
                        userId,
                        request
                );

        LocalDateTime now =
                LocalDateTime.now();

        user.setCreatedAt(now);

        user.setUpdatedAt(now);

        User savedUser =
                userRepository.save(user);

        return userMapper.toResponse(
                savedUser
        );
    }


    // ============================================================
    // GET USER BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public UserResponse getById(
            Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"
                                )
                        );

        return userMapper.toResponse(
                user
        );
    }


    // ============================================================
    // GET ALL USERS
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }


    // ============================================================
    // GET CUSTOMER PHONE
    // INTERNAL SERVICE USE
    // ============================================================

    @Transactional(readOnly = true)
    public String getCustomerPhone(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"
                                )
                        );

        return user.getPhone();
    }


    // ============================================================
    // UPDATE USER
    // ============================================================

    public UserResponse updateUser(
            Long authenticatedUserId,
            Long id,
            UserRequest request) {

        if (!authenticatedUserId.equals(id)) {

            throw new AccessDeniedException(
                    "You are not allowed to update this user"
            );
        }

        User existingUser =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"
                                )
                        );

        userMapper.updateEntity(
                existingUser,
                request
        );

        existingUser.setUpdatedAt(
                LocalDateTime.now()
        );

        User updatedUser =
                userRepository.save(
                        existingUser
                );

        return userMapper.toResponse(
                updatedUser
        );
    }


    // ============================================================
    // DELETE USER
    // ============================================================

    public void deleteUser(
            Long authenticatedUserId,
            Long id) {

        if (!authenticatedUserId.equals(id)) {

            throw new AccessDeniedException(
                    "You are not allowed to delete this user"
            );
        }

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"
                                )
                        );

        userRepository.delete(user);
    }


    // ============================================================
    // GET ONBOARDING STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(
            Long userId) {

        var userOptional =
                userRepository.findById(userId);

        // No profile means onboarding has not started/completed.
        if (userOptional.isEmpty()) {

            return new OnboardingStatusResponse(
                    false,
                    List.of(
                            "firstName",
                            "lastName"
                    )
            );
        }

        User user =
                userOptional.get();

        List<String> missingFields =
                new ArrayList<>();

        if (user.getFirstName() == null
                || user.getFirstName().isBlank()) {

            missingFields.add("firstName");
        }

        if (user.getLastName() == null
                || user.getLastName().isBlank()) {

            missingFields.add("lastName");
        }

        return new OnboardingStatusResponse(
                missingFields.isEmpty(),
                missingFields
        );
    }


    // ============================================================
    // GET ACCOUNT SETTINGS
    // ============================================================

    @Transactional(readOnly = true)
    public AccountSettingsResponse getAccountSettings(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"
                                )
                        );

        boolean onboardingCompleted =
                user.getFirstName() != null
                        && !user.getFirstName().isBlank()
                        && user.getLastName() != null
                        && !user.getLastName().isBlank();

        return new AccountSettingsResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                onboardingCompleted
        );
    }


    // ============================================================
    // UPDATE ACCOUNT SETTINGS
    // ============================================================

    public AccountSettingsResponse updateAccountSettings(
            Long userId,
            AccountSettingsRequest request) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"
                                )
                        );

        user.setFirstName(
                request.firstName().trim()
        );

        user.setLastName(
                request.lastName().trim()
        );

        if (request.phone() != null) {

            String phone =
                    request.phone().trim();

            user.setPhone(
                    phone.isBlank()
                            ? null
                            : phone
            );

        } else {

            user.setPhone(null);
        }

        User updatedUser =
                userRepository.save(user);

        boolean onboardingCompleted =
                updatedUser.getFirstName() != null
                        && !updatedUser.getFirstName().isBlank()
                        && updatedUser.getLastName() != null
                        && !updatedUser.getLastName().isBlank();

        return new AccountSettingsResponse(
                updatedUser.getId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhone(),
                onboardingCompleted
        );
    }
}