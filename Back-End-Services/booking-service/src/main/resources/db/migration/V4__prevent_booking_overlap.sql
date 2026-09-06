-- PostgreSQL needs btree_gist so BIGINT equality can be
-- combined with a GiST range-overlap exclusion constraint.
CREATE EXTENSION IF NOT EXISTS btree_gist;


-- ============================================================
-- VALIDATE BOOKING INTERVAL
-- ============================================================

ALTER TABLE service_requests
    ADD CONSTRAINT chk_service_requests_valid_interval
        CHECK (
            requested_start_at IS NULL
                OR requested_end_at IS NULL
                OR requested_start_at < requested_end_at
            );


ALTER TABLE service_requests
    ADD CONSTRAINT ex_service_requests_no_provider_overlap
    EXCLUDE USING gist (
        provider_id WITH =,
        tstzrange(
            requested_start_at,
            requested_end_at,
            '[)'
        ) WITH &&
    )
    WHERE (
        requested_start_at IS NOT NULL
        AND requested_end_at IS NOT NULL
        AND status IN (
            'PENDING',
            'ACCEPTED'
        )
    );