update juror_mod.juror
set reasonable_adj_code = 'N'
where reasonable_adj_code = ' ';

DELETE
FROM juror_mod.t_reasonable_adjustments
where code = ' ';