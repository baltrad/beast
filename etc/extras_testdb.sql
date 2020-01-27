
CREATE OR REPLACE FUNCTION set_seq_to_max()
 RETURNS trigger AS $$
DECLARE
 maxval BIGINT;
 col_ TEXT := TG_ARGV[0];
 seq_ TEXT := TG_TABLE_NAME || '_' || col_ || '_seq';
BEGIN
 EXECUTE 'SELECT MAX('
         || col_ ||
         ') FROM '
         || TG_TABLE_NAME INTO maxval;
 
 IF maxval IS NULL THEN
   maxval=0;
 END IF;
 
 EXECUTE 'ALTER SEQUENCE '
         || seq_
         || ' RESTART WITH '
         || maxval + 1;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql
;

CREATE TRIGGER set_beast_adaptor_id_seq_to_max AFTER INSERT OR UPDATE ON beast_adaptors
  FOR EACH STATEMENT EXECUTE PROCEDURE set_seq_to_max('adaptor_id')
;

CREATE TRIGGER set_beast_rule_id_seq_to_max AFTER INSERT OR UPDATE ON beast_router_rules
  FOR EACH STATEMENT EXECUTE PROCEDURE set_seq_to_max('rule_id')
;

CREATE TRIGGER set_scheduled_jobs_id_seq_to_max AFTER INSERT OR UPDATE OR DELETE ON beast_scheduled_jobs
  FOR EACH STATEMENT EXECUTE PROCEDURE set_seq_to_max('id')
;

CREATE TRIGGER set_beast_filter_id_seq_to_max AFTER INSERT OR UPDATE ON beast_filters
  FOR EACH STATEMENT EXECUTE PROCEDURE set_seq_to_max('filter_id');

CREATE TRIGGER set_beast_authorization_request_id_seq_to_max AFTER INSERT OR UPDATE ON beast_authorization_request
  FOR EACH STATEMENT EXECUTE PROCEDURE set_seq_to_max('id');
