CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE IF NOT EXISTS core.http_connections (
  id           UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  name         VARCHAR(200) NOT NULL,
  strategy     VARCHAR(50),
  api_key          TEXT,
  api_key_header    TEXT,
  grant_type    VARCHAR(50),
  client_id         TEXT,
  username          TEXT,
  password          TEXT,
  token_endpoint     TEXT,
  client_secret      TEXT,
  scope              TEXT,
  code               TEXT,
  code_verifier      TEXT,
  redirect_uri   VARCHAR(1000),
  created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_http_connections_name
  ON core.http_connections(name);