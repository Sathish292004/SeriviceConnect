
-- Stores the catalog price at the exact moment
-- the customer creates the service request.
--
-- Future catalog price changes will NOT affect
-- existing bookings.

ALTER TABLE service_requests
    ADD COLUMN price_snapshot NUMERIC(12, 2);

COMMENT ON COLUMN service_requests.price_snapshot IS
    'Catalog price captured when the service request was created';