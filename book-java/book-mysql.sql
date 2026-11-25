-- ============================================================
-- 数字化书籍服务系统 MySQL 8.0 初始化脚本（本地开发用）
-- 用法：mysql -uroot -p < book-mysql.sql
-- 说明：脚本会重建 digital_book 库中的业务表，请勿在有数据的库上直接执行。
-- ============================================================

CREATE DATABASE IF NOT EXISTS `digital_book`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `digital_book`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `user_log`;
DROP TABLE IF EXISTS `borrow_info`;
DROP TABLE IF EXISTS `book_detail`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `role_privilege`;
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `privilege`;
DROP TABLE IF EXISTS `r_role`;
DROP TABLE IF EXISTS `u_user`;
DROP TABLE IF EXISTS `SPRING_SESSION_ATTRIBUTES`;
DROP TABLE IF EXISTS `SPRING_SESSION`;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- Spring Session JDBC 表（spring-session-jdbc 官方 MySQL 结构）
-- ------------------------------------------------------------
CREATE TABLE `SPRING_SESSION`
(
    `PRIMARY_ID`            char(36) not null,
    `SESSION_ID`            char(36) not null,
    `CREATION_TIME`         bigint   not null,
    `LAST_ACCESS_TIME`      bigint   not null,
    `MAX_INACTIVE_INTERVAL` int      not null,
    `EXPIRY_TIME`           bigint   not null,
    `PRINCIPAL_NAME`        varchar(100) null,
    constraint SPRING_SESSION_PK primary key (`PRIMARY_ID`)
) ENGINE = InnoDB
  ROW_FORMAT = DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON `SPRING_SESSION` (`SESSION_ID`);
CREATE INDEX SPRING_SESSION_IX2 ON `SPRING_SESSION` (`EXPIRY_TIME`);
CREATE INDEX SPRING_SESSION_IX3 ON `SPRING_SESSION` (`PRINCIPAL_NAME`);

CREATE TABLE `SPRING_SESSION_ATTRIBUTES`
(
    `SESSION_PRIMARY_ID` char(36)     not null,
    `ATTRIBUTE_NAME`     varchar(200) not null,
    `ATTRIBUTE_BYTES`    blob         not null,
    constraint SPRING_SESSION_ATTRIBUTES_PK primary key (`SESSION_PRIMARY_ID`, `ATTRIBUTE_NAME`),
    constraint SPRING_SESSION_ATTRIBUTES_FK foreign key (`SESSION_PRIMARY_ID`)
        references `SPRING_SESSION` (`PRIMARY_ID`) on delete cascade
) ENGINE = InnoDB
  ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 业务表
-- ------------------------------------------------------------
CREATE TABLE `book`
(
    `book_id`     int auto_increment primary key,
    `book_name`   varchar(50) not null,
    `book_author` varchar(50) not null,
    `isbn`        varchar(20) not null,
    `stock`       int         not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `book_detail`
(
    `book_id`      int         not null primary key,
    `book_name`    varchar(50) not null,
    `book_author`  varchar(50) not null,
    `publisher`    varchar(50) not null,
    `time`         varchar(10) not null,
    `image`        varchar(100) null,
    `introduction` text         null,
    constraint book_detail_book_null_fk foreign key (`book_id`) references `book` (`book_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `u_user`
(
    `user_id`   int auto_increment primary key,
    `user_name` varchar(50)  null,
    `password`  varchar(100) null,
    `age`       int          null,
    `sex`       varchar(5)   null,
    `phone`     varchar(30)  null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `r_role`
(
    `role_id`   tinyint primary key,
    `role_name` varchar(50) null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `user_role`
(
    `id`      int auto_increment primary key,
    `user_id` int      not null,
    `role_id` tinyint  not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `privilege`
(
    `privilege_id`   smallint primary key,
    `privilege_name` varchar(50) null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `role_privilege`
(
    `id`           int auto_increment primary key,
    `role_id`      tinyint  not null,
    `privilege_id` smallint not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `borrow_info`
(
    `user_id`     int         not null,
    `user_name`   varchar(50) not null,
    `book_id`     int         not null,
    `book_name`   varchar(50) not null,
    `borrow_time` bigint      not null,
    `deadline`    bigint      not null,
    primary key (`book_id`, `user_id`),
    constraint borrow_info_book_null_fk foreign key (`book_id`) references `book` (`book_id`),
    constraint borrow_info_user_null_fk foreign key (`user_id`) references `u_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `user_log`
(
    `log_id`      int auto_increment primary key,
    `user_id`     smallint     null,
    `role_id`     tinyint      null,
    `action_time` timestamp    null,
    `action_type` tinyint      null,
    `description` varchar(200) null,
    `user_name`   varchar(50)  null,
    `role_name`   varchar(50)  null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `user_role`
    ADD CONSTRAINT user_role_user_null_fk FOREIGN KEY (`user_id`) REFERENCES `u_user` (`user_id`);
ALTER TABLE `user_role`
    ADD CONSTRAINT user_role_role_null_fk FOREIGN KEY (`role_id`) REFERENCES `r_role` (`role_id`);
ALTER TABLE `role_privilege`
    ADD CONSTRAINT role_privilege_role_null_fk FOREIGN KEY (`role_id`) REFERENCES `r_role` (`role_id`);
ALTER TABLE `role_privilege`
    ADD CONSTRAINT role_privilege_privilege_null_fk FOREIGN KEY (`privilege_id`) REFERENCES `privilege` (`privilege_id`);

-- ------------------------------------------------------------
-- 初始数据（密码为 BCrypt 哈希，登录页默认使用 admin / 123456）
-- ------------------------------------------------------------
INSERT INTO `r_role` (`role_id`, `role_name`)
VALUES (1, '管理员'),
       (2, '读者');

INSERT INTO `privilege` (`privilege_id`, `privilege_name`)
VALUES (1, '查看数据'),
       (2, '增删改数据');

INSERT INTO `role_privilege` (`role_id`, `privilege_id`)
VALUES (1, 1),
       (1, 2),
       (2, 1);

INSERT INTO `u_user` (`user_id`, `user_name`, `password`, `age`, `sex`, `phone`)
VALUES (1, 'admin', '$2a$10$M9n/9O5qXuqtjup4jm3Oz.qj393pQ2eR/fS6/Amkf/MqbxgmjE9/K', 18, '男', '13122223333'),
       (2, 'bob', '$2a$10$FRuiYpdeF.AY98Q0GVJuE.hnYWc/a0K7aJN1LGXDHc.0ewyUSc7I6', 22, '女', '15933334444');

INSERT INTO `user_role` (`user_id`, `role_id`)
VALUES (1, 1),
       (2, 2);

-- ------------------------------------------------------------
-- 触发器：与 GaussDB 版 database.sql 行为保持一致
-- ------------------------------------------------------------
DROP TRIGGER IF EXISTS `delete_book_trigger`;
CREATE TRIGGER `delete_book_trigger`
    BEFORE DELETE
    ON `book`
    FOR EACH ROW
    DELETE
    FROM `book_detail`
    WHERE `book_id` = OLD.`book_id`;

DROP TRIGGER IF EXISTS `delete_borrow_info_trigger`;
CREATE TRIGGER `delete_borrow_info_trigger`
    AFTER DELETE
    ON `borrow_info`
    FOR EACH ROW
    UPDATE `book`
    SET `stock` = `stock` + 1
    WHERE `book_id` = OLD.`book_id`;
