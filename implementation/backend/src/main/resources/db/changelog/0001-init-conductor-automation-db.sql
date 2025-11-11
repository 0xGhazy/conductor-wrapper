CREATE SCHEMA IF NOT EXISTS core;
CREATE TYPE core.datasource_type AS ENUM ('MYSQL', 'POSTGRES', 'ORACLE');

CREATE TABLE IF NOT EXISTS core.datasources (
	"name" varchar(255) NOT NULL,
	url varchar(1024) NOT NULL,
	username varchar(100) NOT NULL,
	"password" varchar(255) NOT NULL,
	"type" core.datasource_type NOT NULL,
	connection_timeout int4 DEFAULT 3000 NOT NULL,
	ideal_timeout int4 DEFAULT 3000 NOT NULL,
	maximum_pool_size int4 DEFAULT 1 NOT NULL,
	"schema" varchar NULL,
	CONSTRAINT datasource_pkey PRIMARY KEY (name),
	created_at timestamp default now() not null,
	updated_at timestamp null
);

CREATE TABLE IF NOT EXISTS core.query_store (
	"name" varchar(255) NOT NULL,
	sql_query text NOT NULL,
	datasource varchar(255) NOT NULL,
	"query_type" varchar(50) NOT NULL,
	timeout_seconds int4 DEFAULT 3000 NOT NULL,
	created_at timestamp default now() not null,
   	updated_at timestamp null,
	CONSTRAINT query_store_pkey PRIMARY KEY (name)
);
ALTER TABLE core.query_store ADD CONSTRAINT query_store_datasources_fk FOREIGN KEY (datasource) REFERENCES core.datasources("name") ON DELETE RESTRICT;

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
CREATE UNIQUE INDEX IF NOT EXISTS ux_http_connections_name ON core.http_connections(name);
