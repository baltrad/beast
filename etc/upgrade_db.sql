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

CREATE OR REPLACE FUNCTION upgrade_beast_composite_rules() RETURNS VOID AS $$
BEGIN
  BEGIN
    ALTER TABLE beast_composite_rules ADD COLUMN timeout INTEGER;
    UPDATE beast_composite_rules SET timeout=900;
    ALTER TABLE beast_composite_rules ALTER COLUMN timeout SET NOT NULL;
  EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'Column beast_composite_rules.timeout already exists';
  END;

  BEGIN
    ALTER TABLE beast_composite_rules ADD COLUMN byscan BOOLEAN;
    UPDATE beast_composite_rules SET byscan=false;
    ALTER TABLE beast_composite_rules ALTER COLUMN byscan SET NOT NULL;
  EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'Column beast_composite_rules.byscan already exists';
  END;
END;
$$ LANGUAGE plpgsql
;

CREATE OR REPLACE FUNCTION create_beast_rule_properties() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_rule_properties';
  IF NOT FOUND THEN
    CREATE TABLE beast_rule_properties (
      rule_id INTEGER NOT NULL REFERENCES beast_router_rules(rule_id),
      key text NOT NULL,
      value text NOT NULL,
      PRIMARY KEY(rule_id, key)
    );
  ELSE
    RAISE NOTICE 'Table beast_rule_properties already exists';
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_fk_to_scheduled_jobs() RETURNS VOID AS $$
BEGIN
  PERFORM true from information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_NAME = 'beast_scheduled_jobs_name_fkey' AND TABLE_NAME='beast_scheduled_jobs';
  IF FOUND THEN
    RAISE NOTICE 'Foreign key constraint already exist from beast_scheduled_jobs to beast_router_rules';
  ELSE
    RAISE NOTICE 'Adding foreign key restriction to beast_scheduled_jobs';
    DELETE FROM beast_scheduled_jobs WHERE NAME NOT IN (SELECT DISTINCT name FROM beast_router_rules);
    ALTER TABLE beast_scheduled_jobs ADD FOREIGN KEY(name)  REFERENCES beast_router_rules(name);
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_filters() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_filters';
  IF NOT FOUND THEN
    create table beast_filters (
      filter_id serial primary key,
      type text not null
    );
  ELSE
    RAISE NOTICE 'Table beast_filters already exists';
  END IF;  
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_beast_attr_filters() RETURNS VOID AS $$
BEGIN
  PERFORM true FROM information_schema.tables WHERE table_name = 'beast_attr_filters';
  IF NOT FOUND THEN
    create table beast_attr_filters (
      filter_id integer primary key references beast_filters(filter_id),
      attr text not null,
      op text not null,
      value_type text not null,
      value text not null
    );
  ELSE
    RAISE NOTICE 'Table beast_attr_filters already exists';
  END IF;  
END;
$$ LANGUAGE plpgsql;

select upgrade_beast_composite_rules();
select create_beast_rule_properties();
select add_fk_to_scheduled_jobs();
select create_beast_filters();
select create_beast_attr_filters();

drop function make_plpgsql();
drop function create_beast_rule_properties();
drop function upgrade_beast_composite_rules();
drop function add_fk_to_scheduled_jobs();
drop function create_beast_filters();
drop function create_beast_attr_filters();
