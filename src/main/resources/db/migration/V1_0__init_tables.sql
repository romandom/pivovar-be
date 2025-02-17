CREATE TABLE ingredients
(
    id        BIGSERIAL PRIMARY KEY,
    mashing   INT NOT NULL,
    lautering INT NOT NULL
);

CREATE TABLE recipes
(
    id            BIGSERIAL PRIMARY KEY,
    name          TEXT NOT NULL,
    style         TEXT NOT NULL,
    wort          INT,
    alcohol       DOUBLE PRECISION,
    ibu           INT,
    ebc           INT,
    mash_type     TEXT NOT NULL CHECK (mash_type IN ('INFUSION', 'DECOCTION')),
    ingredient_id BIGINT UNIQUE,
    CONSTRAINT fk_recipe_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients (id)
);

CREATE TABLE mashing_steps
(
    id          BIGSERIAL PRIMARY KEY,
    recipe_id   BIGINT REFERENCES recipes (id) ON DELETE CASCADE,
    temperature INT NOT NULL,
    step_number INT NOT NULL,
    duration    INT NOT NULL,
    percentage  INT NOT NULL
);

CREATE TABLE hopping_steps
(
    id          BIGSERIAL PRIMARY KEY,
    recipe_id   BIGINT REFERENCES recipes (id) ON DELETE CASCADE,
    step_number INT  NOT NULL,
    name        TEXT NOT NULL,
    weight      INT  NOT NULL,
    time        INT  NOT NULL
);

CREATE TABLE malt
(
    id            BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT REFERENCES ingredients (id) ON DELETE CASCADE,
    name          TEXT             NOT NULL,
    weight        DOUBLE PRECISION NOT NULL
);

CREATE TABLE hops
(
    id            BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT REFERENCES ingredients (id) ON DELETE CASCADE,
    name          TEXT             NOT NULL,
    weight        DOUBLE PRECISION NOT NULL
);

CREATE TABLE brew_sessions
(
    id            BIGSERIAL PRIMARY KEY,
    recipe_id     BIGINT REFERENCES recipes (id) ON DELETE CASCADE,
    start_time    TIMESTAMP NOT NULL,
    end_time      TIMESTAMP,
    status        TEXT      NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    current_step  INT       ,
    brewing_phase TEXT      NOT NULL CHECK (brewing_phase IN ('HEATING', 'DOUGHING', 'LAUTERING', 'MASHING', 'BOILING', 'COOLING'))
);

CREATE TABLE brew_logs
(
    id               BIGSERIAL PRIMARY KEY,
    brew_session_id  BIGINT REFERENCES brew_sessions (id) ON DELETE CASCADE,
    vessel           TEXT      NOT NULL CHECK (vessel IN ('MASHING', 'DOUGHING')),
    measurement_type TEXT      NOT NULL CHECK (measurement_type IN ('TEMPERATURE', 'WEIGHT')),
    temperature      DOUBLE PRECISION,
    weight           DOUBLE PRECISION,
    timestamp        TIMESTAMP NOT NULL
);
