-- Retrieve via "SELECT @@global.time_zone, @@session.time_zone;"
SET time_zone = '{{timezone}}';
SET GLOBAL time_zone = '{{timezone}}';

SET NAMES {{charset}}{{#if collation}} COLLATE {{collation}}{{/if}};

{{#if dropFirst}}DROP DATABASE IF EXISTS `{{dbName}}`;
{{/if}}
CREATE DATABASE IF NOT EXISTS `{{dbName}}`
	DEFAULT CHARACTER SET {{charset}}
	{{#if collation}}DEFAULT COLLATE {{collation}}{{/if}}
;

USE `{{dbName}}`;

GRANT ALL ON `{{dbName}}`.* TO '{{dbAdminName}}'@'{{dbHost}}' IDENTIFIED BY '{{dbAdminPass}}' WITH GRANT OPTION;