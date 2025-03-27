CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE games (
    id uuid PRIMARY KEY,
    width integer NOT NULL,
    height integer NOT NULL,
    mines_count integer NOT NULL,
    field text NOT NULL
);

ALTER TABLE games
ALTER COLUMN id SET DEFAULT uuid_generate_v4();