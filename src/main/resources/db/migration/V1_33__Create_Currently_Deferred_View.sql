-- create the view to show currently deferred jurors

create or replace view juror_mod.currently_deferred
as select j.juror_number, jp."owner", jp.def_date as deferred_to, p.loc_code
from juror_mod.juror j
join juror_mod.juror_pool jp on j.juror_number = jp.juror_number
join juror_mod.pool p on jp.pool_number = p.pool_no
where jp.status = 7 and jp.is_active = true