create schema if not exists core;

CREATE TYPE core.datasource_type AS ENUM (
    'MYSQL',
    'POSTGRES',
    'ORACLE'
);

CREATE TYPE core.query_type AS ENUM ('SELECT', 'UPDATE');

CREATE TABLE core.datasource (
    name VARCHAR(255) PRIMARY KEY,
    url VARCHAR(1024) NOT NULL,
    username varchar(100) not null,
    password VARCHAR(255) NOT NULL,
    type core.datasource_type NOT NULL
);

-- queries mapped to a data source
CREATE TABLE core.query_store (
  name            VARCHAR(255) PRIMARY KEY,
  sql_text        TEXT NOT NULL,
  datasource_name VARCHAR(255) NOT NULL REFERENCES core.datasource(name) ON DELETE RESTRICT,
  query_type core.query_type NOT NULL
);
