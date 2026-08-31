CREATE TABLE carriers
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    contact_info VARCHAR(200),
    active       BOOLEAN      NOT NULL DEFAULT true,
    deleted      BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT now()
);