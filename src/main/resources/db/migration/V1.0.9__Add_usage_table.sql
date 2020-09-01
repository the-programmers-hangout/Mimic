CREATE TABLE IF NOT EXISTS usage (
  id SERIAL NOT NULL PRIMARY KEY,
  command varchar(50) NOT NULL,
  serverid bigint NOT NULL,
  usagetime timestamp NOT NULL
);