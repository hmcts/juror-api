-- procedure for creating new notify_template_mapping table based on the existing table whilst it still exists.

select * into juror_mod.notify_template_mapping from juror_digital.notify_template_mapping;

ALTER TABLE juror_mod.notify_template_mapping
ADD CONSTRAINT notify_template_mapping_pkey PRIMARY KEY (template_id),
ADD CONSTRAINT t_form_attr_fkey FOREIGN KEY (form_type) REFERENCES juror_mod.t_form_attr,
ADD CONSTRAINT notify_template_mapping_template_name_key UNIQUE (template_name);
