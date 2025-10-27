-- اختياري: إنشاء السكيمة
CREATE SCHEMA IF NOT EXISTS core;

-- يتطلب امتلاك نوع UUID (موجود أصلاً) ويمكن استخدام gen_random_uuid() إن أردت قيمة افتراضية
CREATE TABLE IF NOT EXISTS core.http_connections (
  id           UUID PRIMARY KEY,                      -- أو DEFAULT gen_random_uuid()
  name         VARCHAR(200) NOT NULL,
  strategy     VARCHAR(50),                           -- Enum كـ STRING
  api_key          TEXT,
  api_key_header    TEXT,
  grant_type    VARCHAR(50),                          -- Enum كـ STRING
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