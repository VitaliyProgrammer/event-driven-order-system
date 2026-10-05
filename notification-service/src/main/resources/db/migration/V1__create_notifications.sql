CREATE TABLE notifications
(
    id          UUID PRIMARY KEY,
    customer_id UUID         NOT NULL,
    order_id    UUID         NOT NULL,
    event_type  VARCHAR(50)  NOT NULL,
    message     VARCHAR(500) NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_notifications_customer_id_created_at ON notifications (customer_id, created_at DESC);

CREATE TABLE processed_events
(
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);
