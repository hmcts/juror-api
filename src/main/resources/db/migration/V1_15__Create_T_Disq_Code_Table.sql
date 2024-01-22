-- procedure for creating new t_disq_code table based on the existing table whilst it still exists.

select * into juror_mod.t_disq_code from juror.dis_code;

ALTER TABLE juror_mod.t_disq_code
ADD CONSTRAINT t_disq_code_pkey PRIMARY KEY (disq_code);

ALTER TABLE juror_mod.t_disq_code
ALTER COLUMN enabled type boolean USING (enabled::boolean);
