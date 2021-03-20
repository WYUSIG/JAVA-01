create schema hmily_demo;
use hmily_demo;
create table account
(
    id            bigint auto_increment
        primary key,
    user_id       varchar(128) not null,
    balance       decimal      not null comment '用户余额',
    create_time   datetime     not null,
    update_time   datetime     null
) charset = utf8mb4;
create table freeze
(
    id            bigint auto_increment
        primary key,
    user_id       varchar(128) not null,
    freeze       decimal      not null comment '用户冻结余额',
    create_time   datetime     not null,
    update_time   datetime     null
) charset = utf8mb4;

insert  into `account`(`id`,`user_id`,`balance`,`create_time`,`update_time`) values
(1,'10000', 10000000,'2021-3-21 14:54:22',NULL);