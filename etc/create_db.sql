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
  nominal_timeout boolean NOT NULL,
  byscan boolean NOT NULL,
  selection_method integer NOT NULL,
  method text NOT NULL,
  prodpar text NOT NULL,
  applygra boolean NOT NULL,
  ZR_A decimal NOT NULL,
  ZR_b decimal NOT NULL,
  ignore_malfunc boolean NOT NULL,
  ctfilter boolean NOT NULL,
  qitotal_field text,
  quantity text,
  options text,
  qc_mode integer NOT NULL,
  reprocess_quality boolean NOT NULL,
  max_age_limit integer NOT NULL
);

create table beast_composite_sources (
  rule_id integer REFERENCES beast_composite_rules(rule_id),
  source text
);

create table beast_composite_detectors (
  rule_id integer REFERENCES beast_composite_rules(rule_id),
  name text REFERENCES beast_anomaly_detectors(name)
);

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
  yscale decimal NOT NULL,
  options text,
  qc_mode integer NOT NULL  
);

create table beast_site2d_sources (
  rule_id integer REFERENCES beast_site2d_rules(rule_id),
  source text
);

create table beast_site2d_detectors (
  rule_id integer REFERENCES beast_site2d_rules(rule_id),
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
  nominal_timeout boolean NOT NULL,  
  ascending boolean NOT NULL,
  minelev decimal NOT NULL,
  maxelev decimal NOT NULL,
  elangles text,
  adaptive_elangles boolean NOT NULL,
  qc_mode integer NOT NULL
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
  path TEXT,
  use_area_in_path BOOLEAN NOT NULL
);

create table beast_scansun_sources (
  rule_id integer REFERENCES beast_router_rules(rule_id),
  source text
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
  zrb decimal NOT NULL,
  applygra boolean NOT NULL,
  productid TEXT
);

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

create table beast_wrwp_rules (
  rule_id INTEGER PRIMARY KEY REFERENCES beast_router_rules(rule_id),
  interval INTEGER NOT NULL,
  maxheight INTEGER NOT NULL,
  mindistance INTEGER NOT NULL,
  maxdistance INTEGER NOT NULL,
  minelangle DECIMAL NOT NULL,
  maxelangle DECIMAL NOT NULL,
  minvelocitythresh DECIMAL NOT NULL,
  maxvelocitythresh DECIMAL NOT NULL,
  minsamplesizereflectivity INTEGER NOT NULL,
  minsamplesizewind INTEGER NOT NULL,
  fields TEXT NOT NULL
);  

create table beast_wrwp_sources (
  rule_id integer REFERENCES beast_wrwp_rules(rule_id),
  source text
);

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


