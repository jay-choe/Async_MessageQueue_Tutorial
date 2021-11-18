drop table if exists order_log;
drop table if exists product;
create table order_log (id integer not null auto_increment, order_stock integer, product_id integer, product_name varchar(255), product_price bigint, status varchar(255), total_price bigint, primary key (id)) engine=InnoDB;
create table product (id integer not null, name varchar(255), price bigint, stock integer, primary key (id)) engine=InnoDB;