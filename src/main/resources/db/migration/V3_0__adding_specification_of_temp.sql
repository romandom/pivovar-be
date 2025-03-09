ALTER TABLE brew_logs RENAME COLUMN temperature TO mash_temperature;
ALTER TABLE brew_logs ADD COLUMN worth_temperature DOUBLE PRECISION;
ALTER TABLE brew_logs RENAME COLUMN weight TO mash_weight;
ALTER TABLE brew_logs ADD COLUMN worth_weight DOUBLE PRECISION;
ALTER TABLE brew_logs DROP COLUMN vessel;
