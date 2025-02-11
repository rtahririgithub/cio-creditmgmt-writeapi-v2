-------------------------------------------------------------------
-- Script to create database, schema and users.
--
-- Passwords given in script are just for example, 
-- make sure to change before script is executed.
-------------------------------------------------------------------
-- Create database. 
-- 
-- Make sure to verify database name in command below.
-- Execute create database command one by one.
-------------------------------------------------------------------
CREATE DATABASE pg_crproflpr;

-------------------------------------------------------------------
-- Create roles. 
--
-- Roles are global and available across all databases.
-------------------------------------------------------------------
-- roles
CREATE ROLE crproflpr_admin;
CREATE ROLE crproflpr_write;
CREATE ROLE crproflpr_read;

-- users
CREATE ROLE crproflpradm WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflpr_app WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflpr_sup WITH LOGIN PASSWORD 'use_GCP_Secret_manager';

-- grants
GRANT ALL PRIVILEGES ON DATABASE pg_crproflpr TO crproflpr_admin;
GRANT crproflpr_admin TO crproflpradm;
GRANT crproflpr_write TO crproflpr_app;
GRANT crproflpr_read TO crproflpr_sup;

-------------------------------------------------------------------
-- Connect to database.
--
-- Make sure to verify database name in command below.
-- admin user account is used to create tables etc.
-------------------------------------------------------------------
\connect pg_crproflpr crproflpradm

-- Execute script file
\include_relative 2_credit-profile-ddl-create.sql
\include_relative X_DATABASE.SQL
