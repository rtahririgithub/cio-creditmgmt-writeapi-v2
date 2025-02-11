-- Connect to postgres instance. Update details as needed.
psql -h 127.0.0.1 -p 5432 -d postgres -U admin

-- execure script file. Update file path as needed.
\i C:/cio-creditmgmt-writeapi-v2/dbscript/1_credit-profile-db-create-np.sql

-- connect to database as admin user.
\connect pg_crprofldv crprofldvadm

-- connect to database as application user.
\connect pg_crprofldv crprofldv_app

-- connect to database as support user.
\connect pg_crprofldv crprofldv_sup