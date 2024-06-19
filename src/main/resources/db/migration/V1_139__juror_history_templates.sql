alter table juror_mod.juror_history
    add column migrated                  boolean default false,
    add column other_information_support text;

update juror_mod.juror_history
set migrated = false;
alter table juror_mod.t_history_code
    add column template varchar(2000);


update juror_mod.t_history_code
set template = E'Attendance date {other_info_date:d MMM yyyy}\nAttendance audit report {other_info_reference}\nTotal paid {other_information}'
where history_code in ('AEDF');

update juror_mod.t_history_code
set template = E'Attendance date {other_info_date:d MMM yyyy}\nTrial {other_information}\nJury attendance audit report {other_info_reference}'
where history_code in ('AJUR');

update juror_mod.t_history_code
set template = E'Attendance date {other_info_date:d MMM yyyy}\nPool attendance audit report {other_info_reference}'
where history_code in ('APOL');

update juror_mod.t_history_code
set template = E'Attendance date {other_info_date:d MMM yyyy}\nCash audit report report {other_info_reference}\nTotal paid {other_information}'
where history_code in ('CASH');

update juror_mod.t_history_code
set template = E'{other_information}'
where history_code in
      ('CHID', 'PDET', 'RESP', 'RNOT', 'PDEF', 'PEXC', 'RCER', 'POLA', 'POLC', 'POLD', 'POLE', 'POLF', 'POLG', 'POLI',
       'POLX', 'PFTA', 'AUTH', 'PUND', 'RSHC', 'RSUM', 'RRES', 'RNRE', 'AWFI', 'RFTA');

update juror_mod.t_history_code
set template = E'{other_information}\nDeferral Code {other_info_reference}'
where history_code in ('RDDL');

update juror_mod.t_history_code
set template = E'Attendance date {other_info_date:d MMM yyyy}\nAttendance audit report {other_info_reference}\nTotal due {other_information}'
where history_code in ('FADD');

update juror_mod.t_history_code
set template = E'Attendance date {other_info_date:d MMM yyyy}\nEdit audit report ({other_information_support}) {other_info_reference}\nTotal due {other_information}'
where history_code in ('FEDT');

update juror_mod.t_history_code
set template = E'Trial: {other_info_reference}'
where history_code in ('TADD', 'VCRE', 'VRET');


update juror_mod.t_history_code
set template = E'Disqualified code - {other_info_reference}\n{other_information}'
where history_code in ('PDIS');

update juror_mod.t_history_code
set template = E'Reassigned to pool - {other_info_reference}\nAt court {other_information}'
where history_code in ('PREA');

update juror_mod.t_history_code
set template = 'EAttendance date {other_info_date:d MMM yyyy}\nTrial {other_info_reference}\nJury attendance audit report {other_info_reference}'
where history_code in ('AJUR');

update juror_mod.t_history_code
set template = E'Deferred to {other_info_date:d MMM yyyy}\nDeferral reason {other_info_reference}\n{other_information}'
where history_code in ('RDEF');

update juror_mod.t_history_code
set template = E'Withdrawal reason {other_info_reference}\n{other_information}'
where history_code in ('RDIS');

update juror_mod.t_history_code
set template = E'Excusal date {other_info_date:d MMM yyyy}\nExcusal reason {other_info_reference}\n{other_information}'
where history_code in ('REDL');

update juror_mod.t_history_code
set template = E'Excusal date {other_info_date:d MMM yyyy}\nExcusal reason {other_info_reference}\n{other_information}'
where history_code in ('REXC');

update juror_mod.t_history_code
set template = E'Date {other_info_date:d MMM yyyy}\n{other_info_reference} years\n{other_information}'
where history_code in ('REXE');

update juror_mod.t_history_code
set template = E'Postponement Date {other_info_date:d MMM yyyy}\nDeferral code: {other_info_reference}\n{other_information}'
where history_code in ('RPST');