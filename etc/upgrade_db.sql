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

CREATE OR REPLACE FUNCTION create_beast_gra_rules() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_gra_rules';
  IF NOT FOUND THEN
    create table beast_gra_rules (
      rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
      area TEXT NOT NULL,
      distancefield TEXT NOT NULL,
      files_per_hour INTEGER NOT NULL,
      acceptable_loss INTEGER NOT NULL,
      object_type TEXT NOT NULL, 
      quantity TEXT NOT NULL,
      zra decimal NOT NULL,
      zrb decimal NOT NULL,
      first_term_utc INTEGER NOT NULL,
      interval INTEGER NOT NULL
    );
  ELSE
    RAISE NOTICE 'Table beast_acrr_rules already exists';
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_wrwp_rules() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_wrwp_rules';
  IF NOT FOUND THEN
    create table beast_wrwp_rules (
      rule_id INTEGER PRIMARY KEY REFERENCES beast_router_rules(rule_id),
      interval INTEGER NOT NULL,
      maxheight INTEGER NOT NULL,
      mindistance INTEGER NOT NULL,
      maxdistance INTEGER NOT NULL,
      minelangle DECIMAL NOT NULL,
      minvelocitythresh DECIMAL NOT NULL
    );
    create table beast_wrwp_sources (
      rule_id integer REFERENCES beast_wrwp_rules(rule_id),
      source text
    );    
  ELSE
    RAISE NOTICE 'Table beast_wrwp_rules already exists';
  END IF; 
END;
$$ LANGUAGE plpgsql;

select create_beast_gmap_rules();
select create_beast_host_filter();
select create_beast_acrr_rules();
select create_beast_gra_rules();
select create_beast_wrwp_rules();

drop function create_beast_gmap_rules();
drop function create_beast_host_filter();
drop function create_beast_acrr_rules();
drop function create_beast_gra_rules();
drop function create_beast_wrwp_rules();

