-- Flyway migration V4: remember the location prayer times are computed for.
--
-- The location is a single fact about the app, so it lives on the existing single-row app_meta table
-- rather than a new table. ALTER TABLE ADD COLUMN works the same on H2 and PostgreSQL; DOUBLE PRECISION
-- is the portable floating-point type (Postgres has no plain DOUBLE).

ALTER TABLE app_meta ADD COLUMN place_name VARCHAR(200);
ALTER TABLE app_meta ADD COLUMN lat DOUBLE PRECISION;
ALTER TABLE app_meta ADD COLUMN lng DOUBLE PRECISION;
ALTER TABLE app_meta ADD COLUMN tz_offset INTEGER;

UPDATE app_meta
   SET place_name = 'Thane, Maharashtra, India', lat = 19.22, lng = 72.98, tz_offset = 330
 WHERE id = 1;
