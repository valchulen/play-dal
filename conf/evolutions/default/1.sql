# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table geotag (
  id                        bigint not null,
  lat                       float,
  lon                       float,
  incapacidad               varchar(255),
  importancia               integer,
  constraint pk_geotag primary key (id))
;

create sequence geotag_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists geotag;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists geotag_seq;

