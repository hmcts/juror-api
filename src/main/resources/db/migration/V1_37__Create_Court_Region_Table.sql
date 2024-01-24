-- procedure for creating new court_region table based on the existing table whilst it still exists.

select * into juror_mod.court_region from juror_digital.court_region;

ALTER TABLE juror_mod.court_region
 ADD CONSTRAINT court_region_pkey PRIMARY KEY (region_id),
 ADD CONSTRAINT court_region_region_name_key UNIQUE (region_name);
