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

create table s3file (
  id                        varchar(40) not null,
  geotag_id                 bigint not null,
  bucket                    varchar(255),
  name                      varchar(255),
  constraint pk_s3file primary key (id))
;

create table usuario (
  id                        bigint not null,
  constraint pk_usuario primary key (id))
;


create table geotag_usuario (
  geotag_id                      bigint not null,
  usuario_id                     bigint not null,
  constraint pk_geotag_usuario primary key (geotag_id, usuario_id))
;
create sequence geotag_seq;

create sequence usuario_seq;

alter table s3file add constraint fk_s3file_geotag_1 foreign key (geotag_id) references geotag (id) on delete restrict on update restrict;
create index ix_s3file_geotag_1 on s3file (geotag_id);



alter table geotag_usuario add constraint fk_geotag_usuario_geotag_01 foreign key (geotag_id) references geotag (id) on delete restrict on update restrict;

alter table geotag_usuario add constraint fk_geotag_usuario_usuario_02 foreign key (usuario_id) references usuario (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists geotag;

drop table if exists geotag_usuario;

drop table if exists s3file;

drop table if exists usuario;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists geotag_seq;

drop sequence if exists usuario_seq;

