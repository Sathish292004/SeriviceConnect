package com.serviceconnect.provider.repository;

import com.serviceconnect.provider.entity.ProviderPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderPhotoRepository
        extends JpaRepository<ProviderPhoto, Long> {

    List<ProviderPhoto> findByProviderIdOrderByDisplayOrderAsc(
            Long providerId
    );
}