DROP INDEX IF EXISTS idx_support_ticket_customer;
DROP INDEX IF EXISTS idx_support_ticket_agent;
DROP INDEX IF EXISTS idx_support_ticket_status;
DROP INDEX IF EXISTS idx_support_ticket_created_at;
DROP INDEX IF EXISTS idx_support_ticket_priority;

CREATE INDEX idx_support_ticket_customer_created
    ON support_tickets(customer_id, created_at DESC);

CREATE INDEX idx_support_ticket_agent_created
    ON support_tickets(assigned_agent_id, created_at DESC);

CREATE INDEX idx_support_ticket_status_created
    ON support_tickets(status, created_at DESC);