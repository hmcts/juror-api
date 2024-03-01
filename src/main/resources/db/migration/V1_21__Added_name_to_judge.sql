ALTER table juror_mod.judge
    ADD COLUMN name VARCHAR(30) NOT NULL default 'ERROR';
ALTER table juror_mod.judge
    ADD COLUMN is_active bool NOT NULL default false;
ALTER table juror_mod.judge
    ADD COLUMN last_used timestamp(6) NULL;

ALTER table juror_mod.judge
    ADD CONSTRAINT juror_code_owner_unique UNIQUE (code, owner);