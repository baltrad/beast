create table beast_adaptors (
  adaptor_id SERIAL PRIMARY KEY,
  name text not null unique,
  type text not null
);

create table beast_adaptors_xmlrpc (
  adaptor_id integer PRIMARY KEY REFERENCES beast_adaptors(adaptor_id),
  uri text not null,
  timeout integer
);

create table beast_router_rules (
  rule_id SERIAL PRIMARY KEY,
  name text NOT NULL UNIQUE,
  type text NOT NULL, 
  author text NOT NULL, 
  description text NOT NULL, 
  active boolean NOT NULL
);
 
create table beast_router_dest (
  rule_id integer REFERENCES beast_router_rules(rule_id),
  recipient text REFERENCES beast_adaptors(name)
);

create table beast_groovy_rules (
  rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
  definition text NOT NULL
);

create table beast_composite_rules (
  rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
  area text NOT NULL,
  interval integer NOT NULL,
  timeout integer NOT NULL,
  byscan boolean NOT NULL
);

create table beast_composite_sources (
  rule_id integer REFERENCES beast_composite_rules(rule_id),
  source text
);

create table beast_rule_properties (
  rule_id integer NOT NULL REFERENCES beast_router_rules(rule_id),
  key text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY(rule_id, key)
);

create table beast_volume_rules (
  rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
  interval integer NOT NULL,
  timeout integer NOT NULL,
  ascending boolean NOT NULL,
  minelev decimal NOT NULL,
  maxelev decimal NOT NULL
);

create table beast_volume_sources (
  rule_id integer REFERENCES beast_volume_rules(rule_id),
  source text
);

create table beast_scheduled_jobs (
  id SERIAL PRIMARY KEY,
  expression text NOT NULL,
  name text NOT NULL REFERENCES beast_router_rules(name)
);

