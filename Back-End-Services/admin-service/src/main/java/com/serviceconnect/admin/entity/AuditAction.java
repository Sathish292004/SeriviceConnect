package com.serviceconnect.admin.entity;

public enum AuditAction {

    TICKET_CREATED,
    TICKET_UPDATED,
    TICKET_ASSIGNED,
    TICKET_STATUS_CHANGED,

    CUSTOMER_MESSAGE_SENT,
    AGENT_MESSAGE_SENT,
    ADMIN_MESSAGE_SENT
}