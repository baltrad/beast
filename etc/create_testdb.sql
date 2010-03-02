drop table if exists router_dest;
drop table if exists adaptors_xmlrpc;
drop table if exists adaptors; 
drop table if exists router_rules;

create table adaptors (
  adaptor_id SERIAL PRIMARY KEY,
  name text not null unique,
  type text not null
);

create table adaptors_xmlrpc (
  adaptor_id integer PRIMARY KEY REFERENCES adaptors(adaptor_id),
  uri text not null,
  timeout integer
);

create table router_rules (
  name varchar(255) NOT NULL PRIMARY KEY,
  type varchar(255) NOT NULL, 
  author varchar(255) NOT NULL, 
  description varchar(255) NOT NULL, 
  active boolean NOT NULL, 
  definition text NOT NULL
);
 
create table router_dest (
  name varchar(255) NOT NULL,
  recipient varchar(255) NOT NULL
);
  
alter table router_dest add foreign key(name) references router_rules(name);
alter table router_dest add foreign key(recipient) references adaptors(name);
