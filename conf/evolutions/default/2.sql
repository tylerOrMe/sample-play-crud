# --- Sample dataset

# --- !Ups

insert into brand (id,name) values (  1,'Dacia');
insert into brand (id,name) values (  2,'Fiat');
insert into car (id,model,price,brand_id) values (  1,'Punto',14000,2);
insert into car (id,model,price,brand_id) values (  2,'Sandero',12350,1);
# --- !Downs

delete from car;
delete from brand;
