CREATE TABLE support_tickets (
                                 id BIGSERIAL PRIMARY KEY,

                                 ticket_number VARCHAR(30) NOT NULL UNIQUE,

                                 customer_id BIGINT NOT NULL,

                                 assigned_agent_id BIGINT,

                                 subject VARCHAR(200) NOT NULL,

                                 description TEXT NOT NULL,

                                 status VARCHAR(30) NOT NULL,

                                 priority VARCHAR(30) NOT NULL,

                                 category VARCHAR(50) NOT NULL,

                                 created_at TIMESTAMPTZ NOT NULL,

                                 updated_at TIMESTAMPTZ NOT NULL,

                                 resolved_at TIMESTAMPTZ,

                                 version BIGINT NOT NULL
);

CREATE INDEX idx_support_ticket_customer
    ON support_tickets(customer_id);

CREATE INDEX idx_support_ticket_agent
    ON support_tickets(assigned_agent_id);

CREATE INDEX idx_support_ticket_status
    ON support_tickets(status);

CREATE INDEX idx_support_ticket_created_at
    ON support_tickets(created_at);

CREATE INDEX idx_support_ticket_priority
    ON support_tickets(priority);