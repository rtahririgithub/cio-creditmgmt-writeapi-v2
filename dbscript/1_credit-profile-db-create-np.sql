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
CREATE DATABASE pg_crprofldv;
CREATE DATABASE pg_crproflpv;
CREATE DATABASE pg_crproflpt;
CREATE DATABASE pg_crproflps;
CREATE DATABASE pg_crproflst;

-------------------------------------------------------------------
-- Create roles. 
--
-- Roles are global and available across all databases.
-------------------------------------------------------------------
-- roles
CREATE ROLE crprofldv_admin;
CREATE ROLE crprofldv_write;
CREATE ROLE crprofldv_read;
--
CREATE ROLE crproflpv_admin;
CREATE ROLE crproflpv_write;
CREATE ROLE crproflpv_read;
--
CREATE ROLE crproflpt_admin;
CREATE ROLE crproflpt_write;
CREATE ROLE crproflpt_read;
--
CREATE ROLE crproflps_admin;
CREATE ROLE crproflps_write;
CREATE ROLE crproflps_read;
--
CREATE ROLE crproflst_admin;
CREATE ROLE crproflst_write;
CREATE ROLE crproflst_read;

-- users
CREATE ROLE crprofldvadm WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crprofldv_app WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crprofldv_sup WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
--
CREATE ROLE crproflpvadm WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflpv_app WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflpv_sup WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
--
CREATE ROLE crproflptadm WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflpt_app WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflpt_sup WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
--
CREATE ROLE crproflpsadm WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflps_app WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflps_sup WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
--
CREATE ROLE crproflstadm WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflst_app WITH LOGIN PASSWORD 'use_GCP_Secret_manager';
CREATE ROLE crproflst_sup WITH LOGIN PASSWORD 'use_GCP_Secret_manager';

-- grants
GRANT ALL PRIVILEGES ON DATABASE pg_crprofldv TO crprofldv_admin;
GRANT crprofldv_admin TO crprofldvadm;
GRANT crprofldv_write TO crprofldv_app;
GRANT crprofldv_read TO crprofldv_sup;
--
GRANT ALL PRIVILEGES ON DATABASE pg_crproflpv TO crproflpv_admin;
GRANT crproflpv_admin TO crproflpvadm;
GRANT crproflpv_write TO crproflpv_app;
GRANT crproflpv_read TO crproflpv_sup;
--
GRANT ALL PRIVILEGES ON DATABASE pg_crproflpt TO crproflpt_admin;
GRANT crproflpt_admin TO crproflptadm;
GRANT crproflpt_write TO crproflpt_app;
GRANT crproflpt_read TO crproflpt_sup;
--
GRANT ALL PRIVILEGES ON DATABASE pg_crproflps TO crproflps_admin;
GRANT crproflps_admin TO crproflpsadm;
GRANT crproflps_write TO crproflps_app;
GRANT crproflps_read TO crproflps_sup;
--
GRANT ALL PRIVILEGES ON DATABASE pg_crproflst TO crproflst_admin;
GRANT crproflst_admin TO crproflstadm;
GRANT crproflst_write TO crproflst_app;
GRANT crproflst_read TO crproflst_sup;

-------------------------------------------------------------------
-- Connect to database.
--
-- Make sure to verify database name in command below.
-- admin user account is used to create tables etc.
-------------------------------------------------------------------
\connect pg_crprofldv crprofldvadm

-- Execute script file.
\include_relative 2_credit-profile-ddl-create.sql
\include_relative X_DATABASE.SQL

-- Grant permissions to users on tables and schema.
GRANT ALL ON SCHEMA CRPROFL TO crprofldv_admin;
GRANT ALL ON ALL TABLES IN SCHEMA CRPROFL TO crprofldv_admin;
GRANT ALL ON SCHEMA CRPROFL TO crprofldv_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA CRPROFL TO crprofldv_write;
GRANT USAGE ON SCHEMA CRPROFL TO crprofldv_read;
GRANT SELECT ON ALL TABLES IN SCHEMA CRPROFL TO crprofldv_read;
-------------------------------------------------------------------
-- Connect to database.
-------------------------------------------------------------------
\connect pg_crproflpv crproflpvadm

-- Execute script file
\include_relative 2_credit-profile-ddl-create.sql
\include_relative X_DATABASE.SQL

-- Grant permissions to users on tables and schema.
GRANT ALL ON SCHEMA CRPROFL TO crproflpv_admin;
GRANT ALL ON ALL TABLES IN SCHEMA CRPROFL TO crproflpv_admin;
GRANT ALL ON SCHEMA CRPROFL TO crproflpv_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA CRPROFL TO crproflpv_write;
GRANT USAGE ON SCHEMA CRPROFL TO crproflpv_read;
GRANT SELECT ON ALL TABLES IN SCHEMA CRPROFL TO crproflpv_read;
-------------------------------------------------------------------
-- Connect to database.
-------------------------------------------------------------------
\connect pg_crproflpt crproflptadm

-- Execute script file
\include_relative 2_credit-profile-ddl-create.sql
\include_relative X_DATABASE.SQL

-- Grant permissions to users on tables and schema.
GRANT ALL ON SCHEMA CRPROFL TO crproflpt_admin;
GRANT ALL ON ALL TABLES IN SCHEMA CRPROFL TO crproflpt_admin;
GRANT ALL ON SCHEMA CRPROFL TO crproflpt_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA CRPROFL TO crproflpt_write;
GRANT USAGE ON SCHEMA CRPROFL TO crproflpt_read;
GRANT SELECT ON ALL TABLES IN SCHEMA CRPROFL TO crproflpt_read;
-------------------------------------------------------------------
-- Connect to database.
-------------------------------------------------------------------
\connect pg_crproflps crproflpsadm

-- Execute script file
\include_relative 2_credit-profile-ddl-create.sql
\include_relative X_DATABASE.SQL

-- Grant permissions to users on tables and schema.
GRANT ALL ON SCHEMA CRPROFL TO crproflps_admin;
GRANT ALL ON ALL TABLES IN SCHEMA CRPROFL TO crproflps_admin;
GRANT ALL ON SCHEMA CRPROFL TO crproflps_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA CRPROFL TO crproflps_write;
GRANT USAGE ON SCHEMA CRPROFL TO crproflps_read;
GRANT SELECT ON ALL TABLES IN SCHEMA CRPROFL TO crproflps_read;
-------------------------------------------------------------------
-- Connect to database.
-------------------------------------------------------------------
\connect pg_crproflst crproflstadm

-- Execute script file
\include_relative 2_credit-profile-ddl-create.sql
\include_relative X_DATABASE.SQL

-- Grant permissions to users on tables and schema.
GRANT ALL ON SCHEMA CRPROFL TO crproflst_admin;
GRANT ALL ON ALL TABLES IN SCHEMA CRPROFL TO crproflst_admin;
GRANT ALL ON SCHEMA CRPROFL TO crproflst_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA CRPROFL TO crproflst_write;
GRANT USAGE ON SCHEMA CRPROFL TO crproflst_read;
GRANT SELECT ON ALL TABLES IN SCHEMA CRPROFL TO crproflst_read;
