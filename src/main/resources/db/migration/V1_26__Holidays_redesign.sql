ALTER table juror_mod.holiday
    RENAME COLUMN owner to loc_code;
ALTER table juror_mod.holiday
    DROP CONSTRAINT holiday_loc_code_fk;
ALTER table juror_mod.holiday
    ADD CONSTRAINT holiday_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location (loc_code)