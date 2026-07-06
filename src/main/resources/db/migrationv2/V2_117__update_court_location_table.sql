ALTER TABLE juror_mod.court_location
    ADD COLUMN digital_by_default BOOLEAN NOT NULL DEFAULT false;

-- set the initial digital by default courts
update juror_mod.court_location set digital_by_default = true where loc_code in ('419','423','431')
