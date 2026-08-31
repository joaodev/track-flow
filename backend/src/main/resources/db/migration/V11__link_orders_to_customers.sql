ALTER TABLE orders DROP COLUMN customer_name;
ALTER TABLE orders ADD COLUMN customer_id BIGINT NOT NULL REFERENCES customers(id) default 0;