-- Premenujeme stĺpec name na stepNumber
ALTER TABLE recipe_steps RENAME COLUMN name TO stepNumber;

-- Zmeníme typ stĺpca na INT
ALTER TABLE recipe_steps ALTER COLUMN stepNumber TYPE INT USING stepNumber::INTEGER;

-- Nastavíme stĺpec ako NOT NULL
ALTER TABLE recipe_steps ALTER COLUMN stepNumber SET NOT NULL;
