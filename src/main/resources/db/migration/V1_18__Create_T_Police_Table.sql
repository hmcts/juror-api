-- procedure for creating new t_police table based on the existing table whilst it still exists.

select * into juror_mod.t_police from juror.t_police;

ALTER TABLE juror_mod.t_police
RENAME COLUMN police_check TO code;

ALTER TABLE juror_mod.t_police
ADD CONSTRAINT t_police_pkey PRIMARY KEY (code);
