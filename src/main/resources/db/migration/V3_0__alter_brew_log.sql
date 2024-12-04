ALTER TABLE brew_logs
    ADD COLUMN process VARCHAR(255);

ALTER TABLE brew_logs
    ADD CONSTRAINT chk_process CHECK (process IN ('MASHING', 'HOPPING'));
