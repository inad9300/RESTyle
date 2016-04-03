SET NAMES {{charset}}{{#if collation}} COLLATE {{collation}}{{/if}};

CREATE DATABASE IF NOT EXISTS `{{dbName}}`
	DEFAULT CHARACTER SET {{charset}}
	{{#if collation}}DEFAULT COLLATE {{collation}}{{/if}}
;

USE `{{dbName}}`;

GRANT ALL ON `{{dbName}}`.* TO '{{dbAdminName}}'@'{{dbHost}}' IDENTIFIED BY '{{dbAdminPass}}' WITH GRANT OPTION;