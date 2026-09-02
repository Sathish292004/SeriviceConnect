package com.serviceconnect.user.controller;

import com.serviceconnect.user.dto.request.UserRequest;
import com.serviceconnect.user.dto.response.ErrorResponse;
import com.serviceconnect.user.dto.response.OnboardingStatusResponse;
import com.serviceconnect.user.dto.response.UserResponse;
import com.serviceconnect.user.dto.response.ValidationErrorResponse;
import com.serviceconnect.user.service.UserService;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
        name = "User Management",
        description = "APIs for managing user profiles"
)
public class UserController {

    private final UserService userService;


    // ============================================================
    // CREATE USER PROFILE
    // ============================================================

    @Operation(
            summary = "Create user profile",
            description = "Creates a profile for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User profile created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User profile already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return userService.createUser(
                userId,
                request
        );
    }


    // ============================================================
    // GET ALL USERS
    // ============================================================

    @Operation(
            summary = "Get all user profiles",
            description = "Returns all user profiles"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User profiles retrieved successfully"
    )
    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }


    // ============================================================
    // GET MY PROFILE
    // ============================================================

    @Operation(
            summary = "Get my profile",
            description = "Returns the profile of the currently authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/me")
    public UserResponse getMyProfile(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return userService.getById(userId);
    }

    // ============================================================
    // GET MY ONBOARDING STATUS
    // ============================================================

    @Operation(
            summary = "Get my onboarding status",
            description = "Returns whether the currently authenticated user has completed onboarding"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Onboarding status retrieved successfully"
            )
    })
    @GetMapping("/me/onboarding")
    public OnboardingStatusResponse getMyOnboardingStatus(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return userService.getOnboardingStatus(userId);
    }


    // ============================================================
    // UPDATE MY PROFILE
    // ============================================================

    @Operation(
            summary = "Update my profile",
            description = "Updates the profile of the currently authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/me")
    public UserResponse updateMyProfile(
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return userService.updateUser(
                userId,
                userId,
                request
        );
    }


    // ============================================================
    // GET USER BY ID
    // ============================================================

    @Operation(
            summary = "Get user profile by ID",
            description = "Returns a user profile using the user ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public UserResponse getById(
            @Parameter(
                    description = "Unique user ID from Auth Service",
                    example = "6"
            )
            @PathVariable Long id) {

        return userService.getById(id);
    }


    // ============================================================
    // UPDATE USER PROFILE BY ID
    // ============================================================

    @Operation(
            summary = "Update user profile",
            description = "Updates an existing user's profile information"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @Parameter(
                    description = "Unique user ID from Auth Service",
                    example = "6"
            )
            @PathVariable Long id,

            @Valid @RequestBody UserRequest request,

            Authentication authentication) {

        Long authenticatedUserId = getUserId(authentication);

        return userService.updateUser(
                authenticatedUserId,
                id,
                request
        );
    }


    // ============================================================
    // DELETE USER PROFILE
    // ============================================================

    @Operation(
            summary = "Delete user profile",
            description = "Permanently deletes a user profile"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User profile deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @Parameter(
                    description = "Unique user ID from Auth Service",
                    example = "6"
            )
            @PathVariable Long id,

            Authentication authentication) {

        Long authenticatedUserId = getUserId(authentication);

        userService.deleteUser(
                authenticatedUserId,
                id
        );
    }


    // ============================================================
    // GET CUSTOMER PHONE
    // INTERNAL SERVICE USE
    // ============================================================

    @GetMapping("/{id}/phone")
    public String getCustomerPhone(
            @PathVariable Long id) {

        return userService.getCustomerPhone(id);
    }


    // ============================================================
    // JWT USER ID
    // ============================================================

    private Long getUserId(
            Authentication authentication) {

        if (authentication == null) {
            throw new IllegalStateException(
                    "Authentication not found"
            );
        }

        Object details = authentication.getDetails();

        if (details instanceof Long userId) {
            return userId;
        }

        throw new IllegalStateException(
                "User ID not found in authentication"
        );
    }
}