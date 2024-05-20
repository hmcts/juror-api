ALTER table juror_mod.financial_audit_details_appearances
    add column loc_code varchar(3) NOT NULL default 'ERR'
;

update juror_mod.financial_audit_details_appearances fada
set loc_code = fad.loc_code
from juror_mod.financial_audit_details fad
where fad.id = fada.financial_audit_id
  and fada.loc_code = 'ERR';


ALTER table juror_mod.financial_audit_details_appearances
    add CONSTRAINT financial_audit_details_appearances_app_loc_code_fk
        FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location (loc_code),
    add CONSTRAINT financial_audit_details_appearances_id_loc_code_unique UNIQUE (financial_audit_id, loc_code, attendance_date, appearance_version)
;
