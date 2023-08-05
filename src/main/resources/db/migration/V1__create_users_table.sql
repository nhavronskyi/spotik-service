CREATE TABLE IF NOT EXISTS users
(
    id            VARCHAR PRIMARY KEY,
    access_token  VARCHAR,
    refresh_token VARCHAR
);