create sequence hibernate_sequence start 1 increment 1;
create table tasks (id int8 not null, description varchar(255), title varchar(255), primary key (id));
create table users (id int8 not null, created_at timestamp, created_by varchar(255), updated_at timestamp, updated_by varchar(255), email varchar(255), first_name varchar(255), is_active boolean, last_name varchar(255), password varchar(255), primary key (id));
create sequence hibernate_sequence start 1 increment 1;
create table tasks (id int8 not null, description varchar(255), title varchar(255), primary key (id));
create table users (id int8 not null, created_at timestamp, created_by varchar(255), updated_at timestamp, updated_by varchar(255), email varchar(255), first_name varchar(255), is_active boolean, last_name varchar(255), password varchar(255), primary key (id));
create sequence hibernate_sequence start 1 increment 1;
create table tasks (id int8 not null, description varchar(255), title varchar(255), primary key (id));
create table users (id int8 not null, created_at timestamp, created_by varchar(255), updated_at timestamp, updated_by varchar(255), email varchar(255), first_name varchar(255), is_active boolean, last_name varchar(255), password varchar(255), primary key (id));
