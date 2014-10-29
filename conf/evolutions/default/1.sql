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
  incapacidad               varchar(255),
  importancia               integer,
  constraint pk_geotag primary key (id))
;

create table s3file (
  id                        varchar(40) not null,
  geotag_id                 bigint not null,
  bucket                    varchar(255),
  name                      varchar(255),
  constraint pk_s3file primary key (id))
;

create sequence admin_seq;

create sequence geotag_seq;

alter table s3file add constraint fk_s3file_geotag_1 foreign key (geotag_id) references geotag (id) on delete restrict on update restrict;
create index ix_s3file_geotag_1 on s3file (geotag_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists admin;

drop table if exists geotag;

drop table if exists s3file;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists admin_seq;

drop sequence if exists geotag_seq;

