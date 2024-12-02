-- Vytvorenie tabuľky recipes
CREATE TABLE recipes (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL UNIQUE
);

-- Vytvorenie tabuľky recipe_steps
CREATE TABLE recipe_steps (
                              id BIGSERIAL PRIMARY KEY,
                              name VARCHAR(255) NOT NULL,
                              target_temperature DOUBLE PRECISION NOT NULL,
                              duration INT NOT NULL,
                              recipe_id BIGINT NOT NULL REFERENCES recipes(id),
                              vessel VARCHAR(50) NOT NULL,
                              is_transfer_step BOOLEAN NOT NULL
);

-- Vytvorenie tabuľky brew_sessions
CREATE TABLE brew_sessions (
                               id BIGSERIAL PRIMARY KEY,
                               start_time TIMESTAMP NOT NULL,
                               end_time TIMESTAMP,
                               status VARCHAR(50) NOT NULL,
                               recipe_id BIGINT NOT NULL REFERENCES recipes(id)
);

-- Vytvorenie tabuľky brew_logs
CREATE TABLE brew_logs (
                           id BIGSERIAL PRIMARY KEY,
                           brew_session_id BIGINT NOT NULL REFERENCES brew_sessions(id),
                           vessel VARCHAR(50) NOT NULL,
                           temperature DOUBLE PRECISION NOT NULL,
                           timestamp TIMESTAMP NOT NULL
);

-- Vytvorenie tabuľky alerts
CREATE TABLE alerts (
                        id BIGSERIAL PRIMARY KEY,
                        brew_session_id BIGINT NOT NULL REFERENCES brew_sessions(id),
                        message VARCHAR(255) NOT NULL,
                        type VARCHAR(50) NOT NULL,
                        priority INT NOT NULL,
                        resolved BOOLEAN NOT NULL DEFAULT FALSE,
                        resolved_at TIMESTAMP
);
