package com.serviceconnect.catalog.repository;

import com.serviceconnect.catalog.entity.CatalogItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CatalogItemRepository
        extends JpaRepository<CatalogItem, Long> {


    // ============================================================
    // EXISTING QUERIES
    // ============================================================

    List<CatalogItem> findByProviderId(
            Long providerId
    );

    List<CatalogItem> findByCategory(
            String category
    );

    List<CatalogItem> findByActiveTrue();

    List<CatalogItem> findByProviderIdAndActiveTrue(
            Long providerId
    );


    // ============================================================
    // PAGINATION - ACTIVE ITEMS
    // ============================================================

    Page<CatalogItem> findByActiveTrue(
            Pageable pageable
    );


    // ============================================================
    // PAGINATION - PROVIDER
    // ============================================================

    Page<CatalogItem> findByProviderIdAndActiveTrue(
            Long providerId,
            Pageable pageable
    );


    // ============================================================
    // PAGINATION - CATEGORY
    // ============================================================

    Page<CatalogItem> findByCategoryAndActiveTrue(
            String category,
            Pageable pageable
    );


    // ============================================================
    // ALL ACTIVE ITEMS FOR APPROVED PROVIDERS
    // ============================================================

    @Query("""
            SELECT c
            FROM CatalogItem c
            WHERE c.active = true
              AND c.providerId IN :providerIds
            """)
    Page<CatalogItem> findActiveForApprovedProviders(
            @Param("providerIds")
            List<Long> providerIds,

            Pageable pageable
    );


    // ============================================================
    // SEARCH ACTIVE ITEMS FOR APPROVED PROVIDERS
    // ============================================================

    @Query("""
            SELECT c
            FROM CatalogItem c
            WHERE c.active = true
              AND c.providerId IN :providerIds
              AND (
                    LOWER(c.name)
                        LIKE LOWER(CONCAT('%', :search, '%'))

                    OR

                    LOWER(c.description)
                        LIKE LOWER(CONCAT('%', :search, '%'))

                    OR

                    LOWER(c.category)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<CatalogItem> searchActiveApprovedProviders(
            @Param("search")
            String search,

            @Param("providerIds")
            List<Long> providerIds,

            Pageable pageable
    );


    // ============================================================
    // SEARCH + CATEGORY
    // ============================================================

    @Query("""
            SELECT c
            FROM CatalogItem c
            WHERE c.active = true
              AND c.providerId IN :providerIds

              AND LOWER(c.category)
                    = LOWER(:category)

              AND (
                    LOWER(c.name)
                        LIKE LOWER(CONCAT('%', :search, '%'))

                    OR

                    LOWER(c.description)
                        LIKE LOWER(CONCAT('%', :search, '%'))

                    OR

                    LOWER(c.category)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<CatalogItem> searchActiveApprovedProvidersByCategory(
            @Param("search")
            String search,

            @Param("category")
            String category,

            @Param("providerIds")
            List<Long> providerIds,

            Pageable pageable
    );


    // ============================================================
    // CATEGORY FOR APPROVED PROVIDERS
    // ============================================================

    @Query("""
            SELECT c
            FROM CatalogItem c
            WHERE c.active = true
              AND c.providerId IN :providerIds
              AND LOWER(c.category)
                    = LOWER(:category)
            """)
    Page<CatalogItem> findActiveByCategoryForApprovedProviders(
            @Param("category")
            String category,

            @Param("providerIds")
            List<Long> providerIds,

            Pageable pageable
    );

}