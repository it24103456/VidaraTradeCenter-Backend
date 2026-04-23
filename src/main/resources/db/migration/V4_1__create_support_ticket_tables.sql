-- V4_1: Create support ticket tables for Story 4.3

-- Support Tickets table
CREATE TABLE support_tickets (
    id              BIGSERIAL       PRIMARY KEY,
    subject         VARCHAR(255)    NOT NULL,
    category        VARCHAR(30)     NOT NULL,
    description     TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    priority        VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM',
    user_id         BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,

    CONSTRAINT fk_support_ticket_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- Indexes for support_tickets
CREATE INDEX idx_support_ticket_user   ON support_tickets(user_id);
CREATE INDEX idx_support_ticket_status ON support_tickets(status);

-- Ticket Messages table
CREATE TABLE ticket_messages (
    id              BIGSERIAL       PRIMARY KEY,
    ticket_id       BIGINT          NOT NULL,
    sender_id       BIGINT          NOT NULL,
    message         TEXT            NOT NULL,
    is_admin_reply  BOOLEAN         NOT NULL DEFAULT FALSE,
    sent_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_message_ticket
        FOREIGN KEY (ticket_id) REFERENCES support_tickets(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ticket_message_sender
        FOREIGN KEY (sender_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- Index for ticket_messages
CREATE INDEX idx_ticket_message_ticket ON ticket_messages(ticket_id);