ALTER TABLE channels ADD COLUMN read_perm boolean NOT NULL default TRUE;
ALTER TABLE channels ADD COLUMN write_perm boolean NOT NULL default FALSE;