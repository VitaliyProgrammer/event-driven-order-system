CREATE TABLE outbox_events
(
    id           UUID PRIMARY KEY,
    aggregate_id UUID        NOT NULL,
    event_type   VARCHAR(50) NOT NULL,
    payload      TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_events_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;
CREATE INDEX idx_outbox_events_aggregate_id ON outbox_events (aggregate_id, created_at DESC);
