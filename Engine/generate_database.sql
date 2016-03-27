SET NAMES utf8mb4 COLLATE utf8_unicode_ci;

CREATE DATABASE IF NOT EXISTS `test_db`
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8_unicode_ci;

USE `test_db`;

CREATE TABLE `book` (
	`author`ENUM()NOT NULL DEFAULT 'Anon.',
	`title`ENUM()NOT NULL
	INDEX (title ASC
	CHECK (author <= 300 AND author >= 1 AND title <= 300 AND title >= 1 AND ([]))
);
COMMENT 'Bunch of pages glued together.'





CREATE TABLE `author` (
	`mail`ENUM()NULL COMMENT ''
	INDEX (
	CHECK (mail <= 0 AND mail >= 0 AND ([]))
);






