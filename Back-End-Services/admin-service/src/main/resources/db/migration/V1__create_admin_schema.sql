CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            actor_id BIGINT NOT NULL,
                            actor_role VARCHAR(50) NOT NULL,
                            action VARCHAR(100) NOT NULL,
                            resource_type VARCHAR(100) NOT NULL,
                            resource_id VARCHAR(100),
                            description TEXT,
                            ip_address INET,
                            user_agent TEXT,
                            created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_actor
    ON audit_logs (actor_id);

CREATE INDEX idx_audit_action
    ON audit_logs (action);

CREATE INDEX idx_audit_resource
    ON audit_logs (resource_type, resource_id);

CREATE INDEX idx_audit_created_at
    ON audit_logs (created_at);