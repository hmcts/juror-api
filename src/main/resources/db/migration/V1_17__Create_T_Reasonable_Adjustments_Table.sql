select * into juror_mod.t_reasonable_adjustments from juror.t_special pt;

alter table juror_mod.t_reasonable_adjustments
rename column spec_need to code;

ALTER TABLE juror_mod.t_reasonable_adjustments
ADD CONSTRAINT t_reasonable_adjustments_pkey PRIMARY KEY (code);
