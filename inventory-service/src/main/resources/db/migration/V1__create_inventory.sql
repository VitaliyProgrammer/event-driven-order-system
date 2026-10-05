CREATE TABLE products
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255)   NOT NULL,
    price      NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    stock      INTEGER        NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMPTZ    NOT NULL
);

CREATE TABLE reservations
(
    id         UUID PRIMARY KEY,
    order_id   UUID        NOT NULL,
    product_id UUID        NOT NULL REFERENCES products (id),
    quantity   INTEGER     NOT NULL CHECK (quantity > 0),
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (order_id, product_id)
);

CREATE TABLE processed_events
(
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);
