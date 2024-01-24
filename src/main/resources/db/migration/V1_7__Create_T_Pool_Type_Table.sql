-- procedure for creating new Pool_Type table, t_pool_type

select * into juror_mod.t_pool_type from juror.pool_type pt;

ALTER TABLE juror_mod.t_pool_type
ADD CONSTRAINT t_pool_type_pkey PRIMARY KEY (pool_type);
