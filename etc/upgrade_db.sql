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

select upgrade_beast_composite_rules();
select create_beast_rule_properties();

drop function make_plpgsql();
drop function upgrade_beast_composite_rules();
