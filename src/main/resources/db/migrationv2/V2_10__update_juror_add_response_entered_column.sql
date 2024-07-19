ALTER TABLE juror_mod.juror_pool ADD COLUMN response_entered BOOLEAN DEFAULT FALSE NOT NULL;

UPDATE juror_mod.juror_pool SET response_entered = TRUE WHERE status NOT IN (1, 9);
