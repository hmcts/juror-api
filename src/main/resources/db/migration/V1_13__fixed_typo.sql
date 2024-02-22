ALTER TABLE juror_mod.juror
    RENAME COLUMN claiming_substance_allowance to claiming_subsistence_allowance;
ALTER TABLE juror_mod.juror_audit
    RENAME COLUMN claiming_substance_allowance to claiming_subsistence_allowance;


ALTER TABLE juror_mod.court_location
    RENAME COLUMN rate_substance_long_day to rate_subsistence_long_day;
ALTER TABLE juror_mod.court_location
    RENAME COLUMN rate_substance_standard to rate_subsistence_standard;

ALTER TABLE juror_mod.court_location_audit
    RENAME COLUMN rate_substance_long_day to rate_subsistence_long_day;
ALTER TABLE juror_mod.court_location_audit
    RENAME COLUMN rate_substance_standard to rate_subsistence_standard;
