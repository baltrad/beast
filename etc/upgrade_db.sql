CREATE OR REPLACE FUNCTION make_plpgsql()
RETURNS VOID
  LANGUAGE SQL
AS $$
  CREATE LANGUAGE plpgsql;
$$;

SELECT
  CASE
    WHEN EXISTS (
      SELECT 1 from pg_catalog.pg_language where lanname='plpgsql'
    ) THEN 
      NULL
    ELSE make_plpgsql()
  END;

CREATE OR REPLACE FUNCTION create_beast_gmap_rules() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_gmap_rules';
  IF NOT FOUND THEN
    create table beast_gmap_rules (
      rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
      area TEXT NOT NULL,
      path TEXT
    );
  ELSE
    RAISE NOTICE 'Table beast_gmap_rules already exists';
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_host_filter() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_host_filter';
  IF NOT FOUND THEN
    create table beast_host_filter (
      name text PRIMARY KEY NOT NULL
    );
  ELSE
    RAISE NOTICE 'Table beast_host_filter already exists';
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_acrr_rules() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_acrr_rules';
  IF NOT FOUND THEN
    create table beast_acrr_rules (
      rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
      area TEXT NOT NULL,
      distancefield TEXT NOT NULL,
      files_per_hour INTEGER NOT NULL,
      hours INTEGER NOT NULL,
      acceptable_loss INTEGER NOT NULL,
      object_type TEXT NOT NULL, 
      quantity TEXT NOT NULL,
      zra decimal NOT NULL,
      zrb decimal NOT NULL
    );
  ELSE
    RAISE NOTICE 'Table beast_acrr_rules already exists';
  END IF; 
END;
$$ LANGUAGE plpgsql;


select create_beast_gmap_rules();
select create_beast_host_filter();
select create_beast_acrr_rules();

drop function create_beast_gmap_rules();
drop function create_beast_host_filter();
drop function create_beast_acrr_rules();

