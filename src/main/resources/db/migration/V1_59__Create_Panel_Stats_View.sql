-- create a view displaying the total number of jurors requested per location and
-- trial, and the number of these jurors assigned the result "J".

create or replace view juror_mod.panel_stats_view
as select
trial_number,
loc_code,
count (*) as jurors_requested,
count (*) filter (where "result" = 'J') as jury_count
from juror_mod.juror_trial
group by trial_number, loc_code;