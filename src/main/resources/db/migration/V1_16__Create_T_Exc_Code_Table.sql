-- procedure for creating new t_exc_code table based on the existing table whilst it still exists.

select * into juror_mod.t_exc_code from juror.exc_code;

ALTER TABLE juror_mod.t_exc_code
ADD CONSTRAINT t_exc_code_pkey PRIMARY KEY (exc_code),
ADD COLUMN for_excusal boolean DEFAULT FALSE,
ADD COLUMN for_deferral boolean DEFAULT FALSE,
ALTER COLUMN by_right type boolean USING (by_right::boolean),
ALTER COLUMN enabled type boolean USING (enabled::boolean);

