ALTER TABLE brew_logs
    RENAME COLUMN worth_weight TO worth_height;

ALTER TABLE brew_logs
ALTER COLUMN worth_height TYPE INT USING worth_height::INT;
