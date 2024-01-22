-- procedure for creating new t_history_code table based on the existing table whilst it still exists.

select * into juror_mod.t_history_code from juror.t_history_code pt;

ALTER TABLE juror_mod.t_history_code
ADD CONSTRAINT t_history_code_pkey PRIMARY KEY (history_code);
