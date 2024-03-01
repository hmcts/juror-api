ALTER table juror_mod.courtroom
    RENAME COLUMN owner to loc_code;
ALTER table juror_mod.courtroom
    ADD CONSTRAINT courtroom_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location (loc_code)
