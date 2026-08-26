package com.serviceconnect.provider.repository;

import com.serviceconnect.provider.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository
        extends JpaRepository<Provider, Long> {

    boolean existsByUserId(Long userId);

    Optional<Provider> findByUserId(Long userId);

    List<Provider> findByStatus(String status);
}