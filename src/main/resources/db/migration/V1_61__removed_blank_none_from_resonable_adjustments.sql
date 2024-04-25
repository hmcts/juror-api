update juror_mod.juror
set reasonable_adj_code = 'N'
where reasonable_adj_code = ' ';

update juror_mod.juror_reasonable_adjustment
set reasonable_adjustment = 'N'
where reasonable_adjustment = ' ';

DELETE
FROM juror_mod.t_reasonable_adjustments
where code = ' ';