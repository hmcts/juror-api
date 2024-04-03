-- drop stats_not_responded_totals view (removing dependency to allow table updates)
drop view if exists juror_dashboard.stats_not_responded_totals;

-- update stats_deferrals
alter table juror_dashboard.stats_deferrals
rename column exec_code to exc_code;

alter table juror_dashboard.stats_deferrals
rename column excusal_count to deferral_code;


-- update stats_excusals
alter table juror_dashboard.stats_excusals
rename column exec_code to exc_code;


-- update stats_not_responded
alter table juror_dashboard.stats_not_responded
rename column non_responsed_count to not_responded_count;


-- recreate stats_not_responded_totals view (referencing new column name)
create or replace view juror_dashboard.stats_not_responded_totals as
select  sum(coalesce(not_responded_count, 0)) as not_responded_count
from    juror_dashboard.stats_not_responded s;