CREATE TABLE order_status_history
(
    id         UUID PRIMARY KEY,
    order_id   UUID        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history (order_id, changed_at);
