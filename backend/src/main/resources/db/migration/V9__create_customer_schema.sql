CREATE TABLE customers
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    phone      VARCHAR(30)  NOT NULL,
    address    VARCHAR(250) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT true,
    deleted    BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);