-- liquibase formatted sql

-- changeset dmitrii:1
create table notification_task
(
  id bigserial primary key,
  message varchar(3000),
  chat_id bigint,
  date_time timestamp
);