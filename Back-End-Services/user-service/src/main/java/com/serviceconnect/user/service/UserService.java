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
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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

            log.warn(
                    "User profile creation rejected: profile already exists, userId={}",
                    userId
            );

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


        log.info(
                "User profile created: userId={}",
                savedUser.getId()
        );


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
                        .orElseThrow(() -> {

                            log.warn(
                                    "User not found: userId={}",
                                    id
                            );

                            return new UserNotFoundException(
                                    "User not found"
                            );
                        });

        return userMapper.toResponse(
                user
        );
    }


    // ============================================================
    // GET ALL USERS
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        List<UserResponse> users =
                userRepository
                        .findAll()
                        .stream()
                        .map(userMapper::toResponse)
                        .toList();


        log.debug(
                "All users retrieved: count={}",
                users.size()
        );


        return users;
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
                        .orElseThrow(() -> {

                            log.warn(
                                    "Customer phone lookup failed: user not found, " +
                                            "userId={}",
                                    userId
                            );

                            return new UserNotFoundException(
                                    "User not found"
                            );
                        });

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

            log.warn(
                    "User update denied: ownership validation failed, " +
                            "authenticatedUserId={}, targetUserId={}",
                    authenticatedUserId,
                    id
            );

            throw new AccessDeniedException(
                    "You are not allowed to update this user"
            );
        }

        User existingUser =
                userRepository.findById(id)
                        .orElseThrow(() -> {

                            log.warn(
                                    "User update failed: user not found, userId={}",
                                    id
                            );

                            return new UserNotFoundException(
                                    "User not found"
                            );
                        });

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


        log.info(
                "User profile updated: userId={}",
                updatedUser.getId()
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

            log.warn(
                    "User deletion denied: ownership validation failed, " +
                            "authenticatedUserId={}, targetUserId={}",
                    authenticatedUserId,
                    id
            );

            throw new AccessDeniedException(
                    "You are not allowed to delete this user"
            );
        }

        User user =
                userRepository.findById(id)
                        .orElseThrow(() -> {

                            log.warn(
                                    "User deletion failed: user not found, userId={}",
                                    id
                            );

                            return new UserNotFoundException(
                                    "User not found"
                            );
                        });

        userRepository.delete(user);


        log.info(
                "User profile deleted: userId={}",
                id
        );
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

            log.debug(
                    "User onboarding status: profile not found, userId={}",
                    userId
            );

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


        log.debug(
                "User onboarding status retrieved: userId={}, completed={}, missingFields={}",
                userId,
                missingFields.isEmpty(),
                missingFields.size()
        );


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
                        .orElseThrow(() -> {

                            log.warn(
                                    "Account settings lookup failed: user not found, " +
                                            "userId={}",
                                    userId
                            );

                            return new UserNotFoundException(
                                    "User not found"
                            );
                        });

        boolean onboardingCompleted =
                user.getFirstName() != null
                        && !user.getFirstName().isBlank()
                        && user.getLastName() != null
                        && !user.getLastName().isBlank();


        log.debug(
                "Account settings retrieved: userId={}, onboardingCompleted={}",
                userId,
                onboardingCompleted
        );


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
                        .orElseThrow(() -> {

                            log.warn(
                                    "Account settings update failed: user not found, " +
                                            "userId={}",
                                    userId
                            );

                            return new UserNotFoundException(
                                    "User not found"
                            );
                        });

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


        log.info(
                "Account settings updated: userId={}, onboardingCompleted={}",
                updatedUser.getId(),
                onboardingCompleted
        );


        return new AccountSettingsResponse(
                updatedUser.getId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhone(),
                onboardingCompleted
        );
    }
}