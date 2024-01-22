-- procedure for creating new t_contact table based on the existing table whilst it still exists.

drop table if exists juror_mod.t_contact;

select * into juror_mod.t_contact from juror.t_phone;

ALTER TABLE juror_mod.t_contact
RENAME COLUMN phone_code TO enquiry_code;

ALTER TABLE juror_mod.t_contact
ADD CONSTRAINT t_contact_pkey PRIMARY KEY (enquiry_code);
