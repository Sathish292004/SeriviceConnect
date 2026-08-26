CREATE TABLE support_messages (
                                  id BIGSERIAL PRIMARY KEY,

                                  ticket_id BIGINT NOT NULL,

                                  sender_id BIGINT NOT NULL,

                                  sender_type VARCHAR(30) NOT NULL,

                                  message TEXT NOT NULL,

                                  created_at TIMESTAMPTZ NOT NULL,

                                  CONSTRAINT fk_support_message_ticket
                                      FOREIGN KEY (ticket_id)
                                          REFERENCES support_tickets(id)
);

CREATE INDEX idx_support_message_ticket
    ON support_messages(ticket_id);

CREATE INDEX idx_support_message_sender
    ON support_messages(sender_id);

CREATE INDEX idx_support_message_created_at
    ON support_messages(created_at);