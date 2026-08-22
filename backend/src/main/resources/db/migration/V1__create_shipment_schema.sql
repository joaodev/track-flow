-- Shipment: the aggregate root. Tracking events are its child records.
CREATE TABLE shipments (
                           id              BIGSERIAL PRIMARY KEY,
                           tracking_code   VARCHAR(20)  NOT NULL UNIQUE,
                           origin          VARCHAR(150) NOT NULL,
                           destination     VARCHAR(150) NOT NULL,
                           carrier         VARCHAR(100) NOT NULL,
                           status          VARCHAR(30)  NOT NULL DEFAULT 'CREATED',
                           created_at      TIMESTAMP    NOT NULL DEFAULT now(),
                           updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- Full chronological history of a shipment's status changes.
CREATE TABLE tracking_events (
                                 id           BIGSERIAL PRIMARY KEY,
                                 shipment_id  BIGINT       NOT NULL REFERENCES shipments(id),
                                 status       VARCHAR(30)  NOT NULL,
                                 location     VARCHAR(150),
                                 description  VARCHAR(500),
                                 occurred_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_tracking_events_shipment ON tracking_events(shipment_id);
CREATE UNIQUE INDEX idx_shipments_tracking_code ON shipments(tracking_code);