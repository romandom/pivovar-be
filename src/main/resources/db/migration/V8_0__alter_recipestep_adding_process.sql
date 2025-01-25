ALTER TABLE recipe_steps
    ADD COLUMN process VARCHAR(255);

ALTER TABLE recipe_steps
    ADD CONSTRAINT chk_process CHECK (process IN ('MASHING', 'HOPPING'));