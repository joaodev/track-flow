-- Order: staff-placed order for products, eventually producing a Shipment.
-- origin/destination are captured at order time (shipping address info);
-- carrier is chosen later, at dispatch time, since carrier availability is
-- a logistics-time decision, not known when the order is placed.
CREATE TABLE orders (
                        id             BIGSERIAL PRIMARY KEY,
                        order_number   VARCHAR(20)   NOT NULL UNIQUE,
                        customer_name  VARCHAR(150)  NOT NULL,
                        origin         VARCHAR(150)  NOT NULL,
                        destination    VARCHAR(150)  NOT NULL,
                        status         VARCHAR(20)   NOT NULL,
                        shipment_id    BIGINT REFERENCES shipments(id),
                        created_at     TIMESTAMP     NOT NULL DEFAULT now(),
                        updated_at     TIMESTAMP     NOT NULL DEFAULT now(),
                        deleted        BOOLEAN       NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX idx_orders_order_number ON orders(order_number);

-- unit_price_at_order snapshots the product's price at order time, so a
-- later price change on the Product never alters historical orders.
CREATE TABLE order_items (
                             id                    BIGSERIAL PRIMARY KEY,
                             order_id              BIGINT  NOT NULL REFERENCES orders(id),
                             product_id            BIGINT  NOT NULL REFERENCES products(id),
                             quantity              INTEGER NOT NULL,
                             unit_price_at_order   NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_order_items_order ON order_items(order_id);