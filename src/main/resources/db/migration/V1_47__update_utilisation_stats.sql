alter table juror_mod.utilisation_stats drop column "owner";
alter table juror_mod.utilisation_stats alter column month_start type date;
