-------------------------------------------------------------------
-- Drop databases.
-------------------------------------------------------------------

-- \connect postgres admin

DROP DATABASE pg_crprofldv with (force);
DROP DATABASE pg_crproflpv with (force);
DROP DATABASE pg_crproflpt with (force);
DROP DATABASE pg_crproflps with (force);
DROP DATABASE pg_crproflst with (force);
--DROP DATABASE pg_crproflpr with (force);

-------------------------------------------------------------------
-- Drop roles.
-------------------------------------------------------------------
DROP ROLE crprofldvadm;
DROP ROLE crprofldv_app;
DROP ROLE crprofldv_sup;
--
DROP ROLE crproflpvadm;
DROP ROLE crproflpv_app;
DROP ROLE crproflpv_sup;
--
DROP ROLE crproflptadm;
DROP ROLE crproflpt_app;
DROP ROLE crproflpt_sup;
--
DROP ROLE crproflpsadm;
DROP ROLE crproflps_app;
DROP ROLE crproflps_sup;
--
DROP ROLE crproflstadm;
DROP ROLE crproflst_app;
DROP ROLE crproflst_sup;
--
--DROP ROLE crproflpradm;
--DROP ROLE crproflpr_app;
--DROP ROLE crproflpr_sup;

-------------------------------------------------------------------
DROP ROLE crprofldv_admin;
DROP ROLE crprofldv_write;
DROP ROLE crprofldv_read;
--
DROP ROLE crproflpv_admin;
DROP ROLE crproflpv_write;
DROP ROLE crproflpv_read;
--
DROP ROLE crproflpt_admin;
DROP ROLE crproflpt_write;
DROP ROLE crproflpt_read;
--
DROP ROLE crproflps_admin;
DROP ROLE crproflps_write;
DROP ROLE crproflps_read;
--
DROP ROLE crproflst_admin;
DROP ROLE crproflst_write;
DROP ROLE crproflst_read;
--
--DROP ROLE crproflpr_admin;
--DROP ROLE crproflpr_write;
--DROP ROLE crproflpr_read;

-------------------------------------------------------------------
-- Script to drop tables and roles.
-------------------------------------------------------------------
DROP TABLE CRPROFL.READDB_SYNC_STATUS;
DROP TABLE CRPROFL.CUSTOMER_CREDIT_PROFILE_REL;
DROP TABLE CRPROFL.CREDIT_WARNING_HISTORY;
DROP TABLE CRPROFL.CREDIT_PROFILE;
DROP TABLE CRPROFL.CUSTOMER;
DROP TABLE CRPROFL.PARTY_CONTACT_MEDIUM;
DROP TABLE CRPROFL.IDENTIFICATION_CHAR_HASH;
DROP TABLE CRPROFL.IDENTIFICATION_CHAR;
DROP TABLE CRPROFL.IDENTIFICATION_ATTRIBUTES;
DROP TABLE CRPROFL.PARTY_IDENTIFICATION;
DROP TABLE CRPROFL.ORGANIZATION;
DROP TABLE CRPROFL.INDIVIDUAL;
DROP TABLE CRPROFL.PARTY;

-- Temp tables
DROP TABLE CRPROFL.X_CREDIT_PROFILE;
DROP TABLE CRPROFL.X_WARNING;
DROP TABLE CRPROFL.X_SYNC_STATUS;
DROP TABLE CRPROFL.X_ASSESSMENT;
DROP TABLE CRPROFL.x_prodqual;

-------------------------------------------------------------------
-- Drop schema.
-------------------------------------------------------------------
DROP SCHEMA CRPROFL;
