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
      zrb decimal NOT NULL,
      applygra boolean NOT NULL,
      productid TEXT
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
    RAISE NOTICE 'Table beast_gra_rules already exists';
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
      ctfilter boolean NOT NULL,
      pcsid text,
      xscale decimal NOT NULL,
      yscale decimal NOT NULL
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

CREATE OR REPLACE FUNCTION update_beast_volume_rules_with_elangles_field() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_volume_rules' AND column_name = 'elangles';
  IF NOT FOUND THEN
    ALTER TABLE beast_volume_rules ADD COLUMN elangles TEXT;
    UPDATE beast_volume_rules SET elangles=NULL;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_acrr_rules_with_applygra() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_acrr_rules' AND column_name = 'applygra';
  IF NOT FOUND THEN
    ALTER TABLE beast_acrr_rules ADD COLUMN applygra boolean;
    UPDATE beast_acrr_rules SET applygra='false';
    ALTER TABLE beast_acrr_rules ALTER COLUMN applygra SET NOT NULL;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_acrr_rules_with_product() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_acrr_rules' AND column_name = 'productid';
  IF NOT FOUND THEN
    ALTER TABLE beast_acrr_rules ADD COLUMN productid TEXT;
  END IF; 
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_quantity() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'quantity';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN quantity text;
    UPDATE beast_composite_rules SET quantity='DBZH';
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_options() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'options';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN options text;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_nominal_timeout() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'nominal_timeout';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN nominal_timeout boolean;
    UPDATE beast_composite_rules SET nominal_timeout='false';
    ALTER TABLE beast_composite_rules ALTER COLUMN nominal_timeout SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_volume_rules_with_nominal_timeout() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_volume_rules' AND column_name = 'nominal_timeout';
  IF NOT FOUND THEN
    ALTER TABLE beast_volume_rules ADD COLUMN nominal_timeout boolean;
    UPDATE beast_volume_rules SET nominal_timeout='false';
    ALTER TABLE beast_volume_rules ALTER COLUMN nominal_timeout SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_volume_rules_with_qc_mode() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_volume_rules' AND column_name = 'qc_mode';
  IF NOT FOUND THEN
    ALTER TABLE beast_volume_rules ADD COLUMN qc_mode integer;
    UPDATE beast_volume_rules SET qc_mode=0;
    ALTER TABLE beast_volume_rules ALTER COLUMN qc_mode SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_volume_rules_with_adaptive_elangles() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_volume_rules' AND column_name = 'adaptive_elangles';
  IF NOT FOUND THEN
    ALTER TABLE beast_volume_rules ADD COLUMN adaptive_elangles boolean;
    UPDATE beast_volume_rules SET adaptive_elangles='false';
    ALTER TABLE beast_volume_rules ALTER COLUMN adaptive_elangles SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_qc_mode() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'qc_mode';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN qc_mode integer;
    UPDATE beast_composite_rules SET qc_mode=0;
    ALTER TABLE beast_composite_rules ALTER COLUMN qc_mode SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_site2d_rules_with_qc_mode() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_site2d_rules' AND column_name = 'qc_mode';
  IF NOT FOUND THEN
    ALTER TABLE beast_site2d_rules ADD COLUMN qc_mode integer;
    UPDATE beast_site2d_rules SET qc_mode=0;
    ALTER TABLE beast_site2d_rules ALTER COLUMN qc_mode SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_site2d_rules_with_options() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_site2d_rules' AND column_name = 'options';
  IF NOT FOUND THEN
    ALTER TABLE beast_site2d_rules ADD COLUMN options text;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_reprocess_quality() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'reprocess_quality';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN reprocess_quality boolean;
    UPDATE beast_composite_rules SET reprocess_quality='false';
    ALTER TABLE beast_composite_rules ALTER COLUMN reprocess_quality SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_wrwp_with_fields() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_wrwp_rules' AND column_name = 'fields';
  IF NOT FOUND THEN
    ALTER TABLE beast_wrwp_rules ADD COLUMN fields TEXT;
    UPDATE beast_wrwp_rules SET fields='';
    ALTER TABLE beast_wrwp_rules ALTER COLUMN fields SET NOT NULL;
  END IF; 
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_composite_rules_with_max_age_limit() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_composite_rules' AND column_name = 'max_age_limit';
  IF NOT FOUND THEN
    ALTER TABLE beast_composite_rules ADD COLUMN max_age_limit integer;
    UPDATE beast_composite_rules SET max_age_limit=-1;
    ALTER TABLE beast_composite_rules ALTER COLUMN max_age_limit SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_authorization() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_authorization';
  IF NOT FOUND THEN
    CREATE TABLE beast_authorization
    (
      nodename VARCHAR(128) UNIQUE NOT NULL,
      nodeemail VARCHAR(256) NOT NULL,
      nodeaddress VARCHAR(256) NOT NULL,
      redirected_address VARCHAR(256), 
      publickey TEXT,
      publickeypath TEXT,
      privatekey TEXT,
      privatekeypath TEXT,
      lastupdated TIMESTAMP,
      authorized BOOLEAN DEFAULT FALSE NOT NULL,
      injector BOOLEAN DEFAULT FALSE NOT NULL,
      local BOOLEAN DEFAULT FALSE,
      connectionuuid VARCHAR(64) NOT NULL PRIMARY KEY
    );
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_authorization_request() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_authorization_request';
  IF NOT FOUND THEN
    CREATE TABLE beast_authorization_request
    (
      id SERIAL PRIMARY KEY NOT NULL,
      nodename VARCHAR(128),
      nodeemail VARCHAR(256),
      nodeaddress VARCHAR(256),
      checksum VARCHAR(64),
      publickey TEXT,
      message TEXT,
      requestuuid VARCHAR(64) NOT NULL,
      outgoing BOOLEAN DEFAULT FALSE,
      remotehost VARCHAR(256),
      receivedat timestamp,
      autorequest BOOLEAN DEFAULT FALSE,
      remoteaddress VARCHAR(256),
      UNIQUE (requestuuid, outgoing)
    );
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_wrwp_rules_with_maxelangle_and_more_attributes() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_wrwp_rules' AND column_name = 'maxelangle';
  IF NOT FOUND THEN
    ALTER TABLE beast_wrwp_rules ADD COLUMN maxelangle DECIMAL;
    ALTER TABLE beast_wrwp_rules ADD COLUMN maxvelocitythresh DECIMAL;
    ALTER TABLE beast_wrwp_rules ADD COLUMN minsamplesizereflectivity INTEGER;
    ALTER TABLE beast_wrwp_rules ADD COLUMN minsamplesizewind INTEGER;
    UPDATE beast_wrwp_rules SET maxelangle=45.0;
    UPDATE beast_wrwp_rules SET maxvelocitythresh=60.0;
    UPDATE beast_wrwp_rules SET minsamplesizereflectivity=40;
    UPDATE beast_wrwp_rules SET minsamplesizewind=40;
    ALTER TABLE beast_wrwp_rules ALTER COLUMN maxelangle SET NOT NULL;
    ALTER TABLE beast_wrwp_rules ALTER COLUMN maxvelocitythresh SET NOT NULL;
    ALTER TABLE beast_wrwp_rules ALTER COLUMN minsamplesizereflectivity SET NOT NULL;
    ALTER TABLE beast_wrwp_rules ALTER COLUMN minsamplesizewind SET NOT NULL;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_wrwp_with_capital_dbzh() RETURNS VOID AS $$
BEGIN
  UPDATE beast_wrwp_rules SET fields = replace(fields, 'dbzh', 'DBZH');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_beast_gmap_rules_with_use_area_in_path() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.columns WHERE table_name = 'beast_gmap_rules' AND column_name = 'use_area_in_path';
  IF NOT FOUND THEN
    ALTER TABLE beast_gmap_rules ADD COLUMN use_area_in_path boolean;
    UPDATE beast_gmap_rules SET use_area_in_path='true';
    ALTER TABLE beast_gmap_rules ALTER COLUMN use_area_in_path SET NOT NULL;
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
select update_beast_volume_rules_with_elangles_field();
select update_beast_acrr_rules_with_applygra();
select update_beast_acrr_rules_with_product();
select update_beast_composite_rules_with_quantity();
select update_beast_composite_rules_with_options();
select update_beast_composite_rules_with_nominal_timeout();
select update_beast_volume_rules_with_nominal_timeout();
select update_beast_volume_rules_with_qc_mode();
select update_beast_volume_rules_with_adaptive_elangles();
select update_beast_composite_rules_with_qc_mode();
select update_beast_site2d_rules_with_qc_mode();
select update_beast_composite_rules_with_reprocess_quality();
select update_beast_wrwp_with_fields();
select update_beast_composite_rules_with_max_age_limit();
select create_beast_authorization();
select create_beast_authorization_request();
select update_beast_wrwp_rules_with_maxelangle_and_more_attributes();
select update_beast_wrwp_with_capital_dbzh();
select update_beast_gmap_rules_with_use_area_in_path();
select update_beast_site2d_rules_with_options();

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
drop function update_beast_volume_rules_with_elangles_field();
drop function update_beast_acrr_rules_with_applygra();
drop function update_beast_acrr_rules_with_product();
drop function update_beast_composite_rules_with_quantity();
drop function update_beast_composite_rules_with_options();
drop function update_beast_composite_rules_with_nominal_timeout();
drop function update_beast_volume_rules_with_nominal_timeout();
drop function update_beast_volume_rules_with_qc_mode();
drop function update_beast_volume_rules_with_adaptive_elangles();
drop function update_beast_composite_rules_with_qc_mode();
drop function update_beast_site2d_rules_with_qc_mode();
drop function update_beast_composite_rules_with_reprocess_quality();
drop function update_beast_wrwp_with_fields();
drop function update_beast_composite_rules_with_max_age_limit();
drop function create_beast_authorization();
drop function create_beast_authorization_request();
drop function update_beast_wrwp_rules_with_maxelangle_and_more_attributes();
drop function update_beast_wrwp_with_capital_dbzh();
drop function update_beast_gmap_rules_with_use_area_in_path();
drop function update_beast_site2d_rules_with_options();
