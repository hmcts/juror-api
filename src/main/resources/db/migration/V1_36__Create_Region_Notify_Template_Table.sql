-- procedure for creating new region_notify_template table based on the existing table whilst it still exists.

select * into juror_mod.region_notify_template from juror_digital.region_notify_template;

ALTER TABLE juror_mod.region_notify_template
 ADD CONSTRAINT region_notify_template_pkey PRIMARY KEY (region_template_id);
