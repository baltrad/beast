
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
 EXECUTE 'ALTER SEQUENCE '
         || seq_
         || ' RESTART WITH '
         || maxval + 1;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql
;

CREATE TRIGGER set_adaptor_id_seq_to_max AFTER INSERT OR UPDATE ON adaptors
  FOR EACH STATEMENT EXECUTE PROCEDURE set_seq_to_max('adaptor_id')
;

