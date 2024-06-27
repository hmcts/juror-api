update juror_mod.t_history_code
set template = E'Attendance date {other_info_date:d MMM yyyy}\nTrial {other_info_reference}\nJury attendance audit report {other_info_reference}'
where history_code in ('AJUR');