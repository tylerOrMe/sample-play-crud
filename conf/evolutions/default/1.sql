# --- First database schema

# --- !Ups

set ignorecase true;

create table brand (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint pk_brand primary key (id))
;

create table car (
  id                        bigint not null,
  model                      varchar(255) not null,
  price                bigint,
  brand_id                bigint,
  constraint pk_car primary key (id))
;

create sequence brand_seq start with 1000;

create sequence car_seq start with 1000;

alter table car add constraint fk_car_brand_1 foreign key (brand_id) references brand (id) on delete restrict on update restrict;
create index ix_car_brand_1 on car (brand_id);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists brand;

drop table if exists car;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists brand_seq;

drop sequence if exists car_seq;

