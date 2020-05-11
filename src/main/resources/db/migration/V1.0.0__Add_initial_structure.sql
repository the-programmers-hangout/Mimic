CREATE TABLE IF NOT EXISTS messages (
  messageid bigint UNIQUE NOT NULL PRIMARY KEY,
  userid bigint NOT NULL,
  content text NOT NULL
);