--liquibase formatted sql

--changeset demo:001-create-port-registry-table
CREATE TABLE port_registry (
    id                 UUID NOT NULL,
    name               VARCHAR(255) NOT NULL,
    category           VARCHAR(100),
    private_ip         VARCHAR(45),
    port               INT NOT NULL,
    description        VARCHAR(1000),
    network_scope      VARCHAR(150) NOT NULL DEFAULT 'default',
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_port_registry PRIMARY KEY (id)
);
--rollback DROP TABLE port_registry;