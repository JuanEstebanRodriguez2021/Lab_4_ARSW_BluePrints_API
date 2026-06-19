CREATE TABLE IF NOT EXISTS blueprint (
    author VARCHAR(100),
    name VARCHAR(100),
    PRIMARY KEY (author, name)
    );

CREATE TABLE IF NOT EXISTS point (
    id SERIAL PRIMARY KEY,
    author VARCHAR(100),
    blueprint_name VARCHAR(100),
    x INTEGER,
    y INTEGER,
    FOREIGN KEY (author, blueprint_name)
    REFERENCES blueprint(author, name)
    );