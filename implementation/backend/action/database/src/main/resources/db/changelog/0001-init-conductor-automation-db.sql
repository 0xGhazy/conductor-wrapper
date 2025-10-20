CREATE TYPE core.datasource_type AS ENUM ('MYSQL', 'POSTGRES', 'ORACLE');
CREATE TYPE core.query_type AS ENUM ('SELECT', 'UPDATE');

CREATE TABLE core.datasource (
	"name" varchar(255) NOT NULL,
	url varchar(1024) NOT NULL,
	username varchar(100) NOT NULL,
	"password" varchar(255) NOT NULL,
	"type" core."datasource_type" NOT NULL,
	connection_timeout int4 DEFAULT 3000 NOT NULL,
	ideal_timeout int4 DEFAULT 3000 NOT NULL,
	maximum_pool_size int4 DEFAULT 1 NOT NULL,
	"schema" varchar NULL,
	CONSTRAINT datasource_pkey PRIMARY KEY (name),
	created_at timestamp default now() not null,
	updated_at timestamp null
);

CREATE TABLE core.query_store (
	"name" varchar(255) NOT NULL,
	sql_query text NOT NULL,
	datasource varchar(255) NOT NULL,
	"query_type" core."query_type" NOT NULL,
	timeout_seconds int4 DEFAULT 3000 NOT NULL,
	CONSTRAINT query_store_pkey PRIMARY KEY (name)
);
ALTER TABLE core.query_store ADD CONSTRAINT query_store_datasource_fk FOREIGN KEY (datasource) REFERENCES core.datasource("name") ON DELETE RESTRICT;




INSERT INTO core.datasource
("name", url, username, "password", "type", connection_timeout, ideal_timeout, "schema", created_at, updated_at, maximum_pool_size)
VALUES('POSTGRES_TESTING', 'jdbc:postgresql://127.0.0.1:55432/automation_test_postgres', 'root', 'root', 'POSTGRES'::core."datasource_type", 3000, 3000, NULL, '2025-09-28 17:15:43.368', NULL, 1);
INSERT INTO core.datasource
("name", url, username, "password", "type", connection_timeout, ideal_timeout, "schema", created_at, updated_at, maximum_pool_size)
VALUES('POSTGRES_TESTING_INACTIVE', 'jdbc:postgresql://127.0.0.1:55432/automation_test_postgres', 'root', 'root', 'POSTGRES'::core."datasource_type", 3000, 3000, NULL, '2025-09-28 17:15:43.368', '2025-09-28 17:51:57.369', 1);
INSERT INTO core.datasource
("name", url, username, "password", "type", connection_timeout, ideal_timeout, "schema", created_at, updated_at, maximum_pool_size)
VALUES('POSTGRES_TESTING_REG', 'jdbc:postgresql://127.0.0.1:55432/automation_test_postgres', 'root', 'root', 'POSTGRES'::core."datasource_type", 5000, 5000, NULL, '2025-09-28 18:03:00.042', NULL, 1);




INSERT INTO core.query_store
("name", sql_query, datasource, "query_type", timeout_seconds)
VALUES('insert10million', 'DO $$
DECLARE
    batch INT := 1;
    total_batches INT := 2;
    rows_per_batch INT := 5000000;
BEGIN
    FOR batch IN 1..total_batches LOOP
        RAISE NOTICE ''Inserting batch % of %'', batch, total_batches;

        INSERT INTO test.sales_par (sale_date, customer_id, product_id, quantity, amount)
        SELECT
            date ''2016-01-01'' + (random() * 3650)::int,
            (random() * 1000000)::int,
            (random() * 10000)::int,
            (1 + random() * 10)::int,
            round((random() * 500)::numeric, 2)
        FROM generate_series(1, rows_per_batch);

        COMMIT;
    END LOOP;
END$$;', 'POSTGRES_TESTING', 'UPDATE'::core."query_type", 3);