-- Inventory: 1:1 with Product. `version` enables optimistic locking, so two
-- concurrent stock adjustments can't silently overwrite each other.
CREATE TABLE inventory (
                           id                   BIGSERIAL PRIMARY KEY,
                           product_id           BIGINT  NOT NULL UNIQUE REFERENCES products(id),
                           quantity_on_hand     INTEGER NOT NULL DEFAULT 0,
                           quantity_reserved    INTEGER NOT NULL DEFAULT 0,
                           low_stock_threshold  INTEGER NOT NULL DEFAULT 10,
                           version              BIGINT  NOT NULL DEFAULT 0,
                           updated_at           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_inventory_product ON inventory(product_id);