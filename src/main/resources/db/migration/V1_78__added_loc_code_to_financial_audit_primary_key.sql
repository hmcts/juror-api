ALTER table juror_mod.financial_audit_details_appearances
    add column loc_code varchar(3) NOT NULL default 'ERR',
    add CONSTRAINT financial_audit_details_appearances_app_loc_code_fk
        FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location (loc_code),
    add CONSTRAINT financial_audit_details_appearances_id_loc_code_unique UNIQUE (financial_audit_id, loc_code, attendance_date, appearance_version)
;

update juror_mod.financial_audit_details_appearances fada
set loc_code = fad.loc_code
from juror_mod.financial_audit_details fad
where fad.id = fada.financial_audit_id
  and fada.loc_code is null;


ALTER table juror_mod.appearance
    add column f_audit_loc_code varchar(3),
    add CONSTRAINT appearance_f_audit_loc_code_fk
        FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location (loc_code)
;


ALTER table juror_mod.appearance_audit
    add column f_audit_loc_code varchar(3),
    add CONSTRAINT appearance_f_audit_loc_code_fk
        FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location (loc_code)
;

update juror_mod.appearance fada
set f_audit_loc_code = fad.loc_code
from juror_mod.financial_audit_details fad
where fad.id = fada.f_audit
  and fada.f_audit_loc_code is null and fada.f_audit is not null;