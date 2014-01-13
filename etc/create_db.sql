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

create table beast_filters (
  filter_id serial primary key,
  type text not null
);

create table beast_attr_filters (
  filter_id integer primary key references beast_filters(filter_id),
  attr text not null,
  op text not null,
  value_type text not null,
  value text not null,
  negated boolean not null
);

create table beast_combined_filters (
  filter_id integer primary key references beast_filters(filter_id),
  match_type text not null
);

create table beast_combined_filter_children (
  filter_id integer not null references beast_combined_filters(filter_id),
  child_id integer not null references beast_filters(filter_id),
  primary key(filter_id, child_id)
);

create table beast_anomaly_detectors (
  name text PRIMARY KEY NOT NULL,
  description text
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
  byscan boolean NOT NULL,
  selection_method integer NOT NULL,
  method text NOT NULL,
  prodpar text NOT NULL
);

create table beast_composite_sources (
  rule_id integer REFERENCES beast_composite_rules(rule_id),
  source text
);

create table beast_composite_detectors (
  rule_id integer REFERENCES beast_composite_rules(rule_id),
  name text REFERENCES beast_anomaly_detectors(name)
);

create table beast_rule_properties (
  rule_id integer NOT NULL REFERENCES beast_router_rules(rule_id),
  key text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY(rule_id, key)
);

create table beast_rule_filters (
  rule_id integer NOT NULL REFERENCES beast_router_rules(rule_id),
  key text NOT NULL,
  filter_id integer NOT NULL REFERENCES beast_filters(filter_id),
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

create table beast_volume_detectors (
  rule_id integer REFERENCES beast_volume_rules(rule_id),
  name text REFERENCES beast_anomaly_detectors(name)
);

create table beast_gmap_rules (
  rule_id integer PRIMARY KEY REFERENCES beast_router_rules(rule_id),
  area TEXT NOT NULL,
  path TEXT
);

create table beast_scheduled_jobs (
  id SERIAL PRIMARY KEY,
  expression text NOT NULL,
  name text NOT NULL REFERENCES beast_router_rules(name)
);

create table beast_host_filter (
  name text PRIMARY KEY NOT NULL
);

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

create table beast_gra_rules (
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
  first_term_utc INTEGER NOT NULL,
  interval INTEGER NOT NULL
);

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
