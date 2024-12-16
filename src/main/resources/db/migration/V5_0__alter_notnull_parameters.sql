ALTER TABLE recipe_steps ALTER COLUMN recipe_id DROP NOT NULL;

ALTER TABLE brew_sessions ALTER COLUMN recipe_id DROP NOT NULL;

ALTER TABLE brew_logs ALTER COLUMN brew_session_id DROP NOT NULL;

ALTER TABLE alerts ALTER COLUMN brew_session_id DROP NOT NULL;