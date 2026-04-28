CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE datasets (
    id                    VARCHAR(255) PRIMARY KEY,
    catalog_id            VARCHAR(255) NOT NULL,
    published             BOOLEAN NOT NULL DEFAULT FALSE,
    approved              BOOLEAN NOT NULL DEFAULT FALSE,
    last_modified         TIMESTAMP,
    uri                   VARCHAR(500),
    specialized_type      VARCHAR(50),
    application_profile   VARCHAR(50) NOT NULL DEFAULT 'DCAT_AP_NO',
    data                  JSONB
);

CREATE INDEX idx_datasets_catalog_id ON datasets (catalog_id);
CREATE INDEX idx_datasets_catalog_specialized ON datasets (catalog_id, specialized_type);
