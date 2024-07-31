UPDATE juror_mod.t_history_code
SET template = 'Attendance date {other_info_date:d MMM yyyy}
Trial {other_information}
Jury attendance audit report {other_info_reference}'
WHERE history_code = 'AJUR';
