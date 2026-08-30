-- Product: catalog entry. Deactivation (active = false) is the product's
-- end-of-life state — there is no separate soft-delete flag, since a
-- discontinued/inactive product and a "deleted" one are the same thing here.
CREATE TABLE products (
                          id           BIGSERIAL PRIMARY KEY,
                          sku          VARCHAR(50)   NOT NULL UNIQUE,
                          name         VARCHAR(150)  NOT NULL,
                          description  VARCHAR(500),
                          unit_price   NUMERIC(10,2) NOT NULL,
                          active       BOOLEAN       NOT NULL DEFAULT true,
                          created_at   TIMESTAMP     NOT NULL DEFAULT now(),
                          updated_at   TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_products_sku ON products(sku);