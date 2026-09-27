CREATE TABLE orders
(
    id          UUID PRIMARY KEY,
    customer_id UUID        NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    version     BIGINT      NOT NULL
);

-- "My orders, newest first" is the main read query.
CREATE INDEX idx_orders_customer_id_created_at ON orders (customer_id, created_at DESC);

CREATE TABLE order_items
(
    id         UUID PRIMARY KEY,
    order_id   UUID    NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID    NOT NULL,
    quantity   INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
