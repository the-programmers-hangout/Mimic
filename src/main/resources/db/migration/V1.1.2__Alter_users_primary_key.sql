ALTER TABLE messages DROP CONSTRAINT user_fkey;
ALTER TABLE users DROP CONSTRAINT users_pkey;
CREATE UNIQUE INDEX users_pkey ON users USING btree(userid, serverid);
ALTER TABLE messages ADD CONSTRAINT user_fkey FOREIGN KEY (userid, serverid) REFERENCES users (userid, serverid) ON DELETE CASCADE;