package com.serviceconnect.catalog.repository;

import com.serviceconnect.catalog.entity.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogItemRepository
        extends JpaRepository<CatalogItem, Long> {

    List<CatalogItem> findByProviderId(Long providerId);

    List<CatalogItem> findByCategory(String category);

    List<CatalogItem> findByActiveTrue();

    List<CatalogItem> findByProviderIdAndActiveTrue(Long providerId);
}