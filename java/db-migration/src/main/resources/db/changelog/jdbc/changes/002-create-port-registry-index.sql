--liquibase formatted sql

--changeset demo:002-create-port-registry-index
CREATE INDEX idx_registry_scope_ip_port ON port_registry (network_scope, private_ip, port);
--rollback DROP INDEX idx_registry_scope_ip_port;
