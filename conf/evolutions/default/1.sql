# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table admin (
  id                        bigint not null,
  password_length           integer,
  pass                      varchar(255),
  jurisdiccion              varchar(255),
  constraint pk_admin primary key (id))
;

create table geotag (
  id                        bigint not null,
  lat                       float,
  lon                       float,
  usuarios                  varchar(255),
  photo_names               varchar(255),
  incapacidad               varchar(255),
  importancia               integer,
  constraint pk_geotag primary key (id))
;

create sequence admin_seq;

create sequence geotag_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists admin;

drop table if exists geotag;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists admin_seq;

drop sequence if exists geotag_seq;

