CREATE TABLE IF NOT EXISTS server_config (
  serverid bigint NOT NULL PRIMARY KEY,
  opt_in_role VARCHAR(255) NOT NULL
);