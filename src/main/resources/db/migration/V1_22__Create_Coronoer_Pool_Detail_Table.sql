-- procedure for creating new coroner pool detail table in the juror mod schema

select * into juror_mod.coroner_pool_detail from juror.coroner_pool_detail;