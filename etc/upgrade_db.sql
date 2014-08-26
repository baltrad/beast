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

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_applygra() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'applygra';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN applygra boolean;
    UPDATE beast_composite_rules SET applygra='false';
    ALTER TABLE beast_composite_rules ALTER COLUMN applygra SET NOT NULL;
  END IF; 
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'zr_a';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN ZR_A decimal;
    UPDATE beast_composite_rules SET ZR_A=200.0;
    ALTER TABLE beast_composite_rules ALTER COLUMN ZR_A SET NOT NULL;
  END IF; 
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'zr_b';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN ZR_b decimal;
    UPDATE beast_composite_rules SET ZR_b=1.6;
    ALTER TABLE beast_composite_rules ALTER COLUMN ZR_b SET NOT NULL;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_ignore_malfunc() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'ignore_malfunc';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN ignore_malfunc boolean;
    UPDATE beast_composite_rules SET ignore_malfunc='false';
    ALTER TABLE beast_composite_rules ALTER COLUMN ignore_malfunc SET NOT NULL;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_scansun_sources() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_scansun_sources';
  IF NOT FOUND THEN
    create table beast_scansun_sources (
      rule_id integer REFERENCES beast_router_rules(rule_id),
      source text
    );
  ELSE
    RAISE NOTICE 'Table beast_scansun_sources already exists';
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_ctfilter() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'ctfilter';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN ctfilter boolean;
    UPDATE beast_composite_rules SET ctfilter='false';
    ALTER TABLE beast_composite_rules ALTER COLUMN ctfilter SET NOT NULL;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_scansun_sources() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_scansun_sources';
  IF NOT FOUND THEN
    create table beast_scansun_sources (
      rule_id integer REFERENCES beast_router_rules(rule_id),
      source text
    );
  ELSE
    RAISE NOTICE 'Table beast_scansun_sources already exists';
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_qitotal_field() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'qitotal_field';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN qitotal_field TEXT;
    UPDATE beast_composite_rules SET qitotal_field=NULL;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_site2d_rules() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_site2d_rules';
  IF NOT FOUND THEN
    create table beast_site2d_rules (
      rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
      area text,
      interval integer NOT NULL,
      byscan boolean NOT NULL,
      method text NOT NULL,
      prodpar text NOT NULL,
      applygra boolean NOT NULL,
      ZR_A decimal NOT NULL,
      ZR_b decimal NOT NULL,
      ignore_malfunc boolean NOT NULL,
      ctfilter boolean NOT NULL
    );
  ELSE
    RAISE NOTICE 'Table beast_site2d_rules already exists';
  END IF;
  
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_site2d_sources';
  IF NOT FOUND THEN
    create table beast_site2d_sources (
      rule_id integer REFERENCES beast_site2d_rules(rule_id),
      source text
    );
  ELSE
    RAISE NOTICE 'Table beast_site2d_sources already exists';
  END IF; 
  
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_site2d_detectors';
  IF NOT FOUND THEN
    create table beast_site2d_detectors (
      rule_id integer REFERENCES beast_site2d_rules(rule_id),
      name text REFERENCES beast_anomaly_detectors(name)
    );
  ELSE
    RAISE NOTICE 'Table beast_site2d_detectors already exists';
  END IF;   
END;
$$ LANGUAGE plpgsql;

select create_beast_gmap_rules();
select create_beast_host_filter();
select create_beast_acrr_rules();
select create_beast_gra_rules();
select create_beast_wrwp_rules();
select update_beast_composite_rules_with_applygra();
select update_beast_composite_rules_with_ignore_malfunc();
select update_beast_composite_rules_with_ctfilter();
select create_beast_scansun_sources();
select update_beast_composite_rules_with_qitotal_field();
select create_beast_site2d_rules();

drop function create_beast_gmap_rules();
drop function create_beast_host_filter();
drop function create_beast_acrr_rules();
drop function create_beast_gra_rules();
drop function create_beast_wrwp_rules();
drop function update_beast_composite_rules_with_applygra();
drop function update_beast_composite_rules_with_ignore_malfunc();
drop function update_beast_composite_rules_with_ctfilter();
drop function create_beast_scansun_sources();
drop function update_beast_composite_rules_with_qitotal_field();
drop function create_beast_site2d_rules();

