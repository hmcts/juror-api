-- drop views with dependency on renamed columns
drop view if exists juror_mod.mod_juror_detail;
drop view if exists juror_mod.juror_digital_response;
drop view if exists juror_mod.juror_paper_response;

-- update juror table address fields
ALTER TABLE juror_mod.juror
RENAME column address to address_line_1;

ALTER TABLE juror_mod.juror
RENAME column address2 to address_line_2;

ALTER TABLE juror_mod.juror
RENAME column address3 to address_line_3;

ALTER TABLE juror_mod.juror
RENAME column address4 to address_line_4;

ALTER TABLE juror_mod.juror
RENAME column address5 to address_line_5;

ALTER TABLE juror_mod.juror
RENAME column zip to postcode;

ALTER TABLE juror_mod.juror
drop column address6;

-- update juror_audit table address fields
ALTER TABLE juror_mod.juror_audit
RENAME column address to address_line_1;

ALTER TABLE juror_mod.juror_audit
RENAME column address2 to address_line_2;

ALTER TABLE juror_mod.juror_audit
RENAME column address3 to address_line_3;

ALTER TABLE juror_mod.juror_audit
RENAME column address4 to address_line_4;

ALTER TABLE juror_mod.juror_audit
RENAME column address5 to address_line_5;

ALTER TABLE juror_mod.juror_audit
RENAME column zip to postcode;

-- update coroner_pool_detail table address fields
ALTER TABLE juror_mod.coroner_pool_detail
RENAME column address1 to address_line_1;

ALTER TABLE juror_mod.coroner_pool_detail
RENAME column address2 to address_line_2;

ALTER TABLE juror_mod.coroner_pool_detail
RENAME column address3 to address_line_3;

ALTER TABLE juror_mod.coroner_pool_detail
RENAME column address4 to address_line_4;

ALTER TABLE juror_mod.coroner_pool_detail
RENAME column address5 to address_line_5;

-- update juror_response table address fields
ALTER TABLE juror_mod.juror_response
RENAME column address to address_line_1;

ALTER TABLE juror_mod.juror_response
RENAME column address2 to address_line_2;

ALTER TABLE juror_mod.juror_response
RENAME column address3 to address_line_3;

ALTER TABLE juror_mod.juror_response
RENAME column address4 to address_line_4;

ALTER TABLE juror_mod.juror_response
RENAME column address5 to address_line_5;

ALTER TABLE juror_mod.juror_response
RENAME column zip to postcode;

-- update mod_juror_detail view to reference new address field names
CREATE VIEW juror_mod.mod_juror_detail
AS
WITH juror_details_cte AS (
         SELECT j.juror_number,
            COALESCE(s.pool_no, jp.pool_number) AS pool_no,
            COALESCE(s.service_start_date, jp.ret_date) AS ret_date,
            COALESCE(s.loc_code, p.loc_code) AS loc_code,
            j.title,
            j.first_name,
            j.last_name,
            j.address_line_1,
            j.address_line_2,
            j.address_line_3,
            j.address_line_4,
            j.address_line_5,
            j.postcode,
            jp.next_date,
            jp.status,
            j.h_phone AS phone_number,
            j.m_phone AS alt_phone_number,
            j.dob,
            j.notes,
            j.h_email AS email,
            j.last_update,
            r.title AS new_title,
            r.first_name AS new_first_name,
            r.last_name AS new_last_name,
            r.address_line_1 AS new_address_1,
            r.address_line_2 AS new_address_2,
            r.address_line_3 AS new_address_3,
            r.address_line_4 AS new_address_4,
            r.address_line_5 AS new_address_5,
            r.postcode AS new_postcode,
            r.date_received,
            r.processing_status,
            r.phone_number AS new_phone_number,
            r.alt_phone_number AS new_alt_phone_number,
            r.date_of_birth AS new_dob,
            r.email AS new_email,
            r.thirdparty_fname,
            r.thirdparty_lname,
            r.thirdparty_reason,
            r.thirdparty_other_reason,
            r.main_phone,
            r.other_phone,
            r.email_address,
            r.relationship,
            r.residency,
            r.residency_detail,
            r.mental_health_act,
            r.mental_health_act_details,
            r.bail,
            r.bail_details,
            r.convictions,
            r.convictions_details,
            r.deferral_reason,
            r.deferral_date,
            r.reasonable_adjustments_arrangements,
            r.excusal_reason,
            r.processing_complete,
            r.completed_at,
            r.version,
            r.juror_email_details,
            r.juror_phone_details,
            r.staff_login,
            r.staff_assignment_date,
            r.urgent,
            r.super_urgent,
            r.welsh,
            r.reply_type,
            row_number() over (partition by j.juror_number order by jp.ret_date desc) AS row_no
           FROM juror_mod.juror j
             LEFT JOIN juror_mod.juror_pool jp ON j.juror_number = jp.juror_number
             LEFT JOIN juror_mod.pool p ON jp.pool_number = p.pool_no
             LEFT JOIN juror_mod.juror_response r ON r.juror_number = j.juror_number
             LEFT JOIN juror_mod.summons_snapshot s ON r.juror_number = s.juror_number
          WHERE jp.is_active = true
        )
 SELECT j.juror_number,
    j.pool_no,
    j.ret_date,
    c.loc_code,
    c.loc_name,
    c.loc_court_name,
    c.loc_address1,
    c.loc_address2,
    c.loc_address3,
    c.loc_address4,
    c.loc_address5,
    c.loc_address6,
    c.loc_zip,
    c.loc_attend_time,
    j.title,
    j.first_name,
    j.last_name,
    j.address_line_1 as address,
    j.address_line_2 as address2,
    j.address_line_3 as address3,
    j.address_line_4 as address4,
    j.address_line_5 as address5,
    j.postcode as zip,
    j.next_date,
    j.status,
    j.phone_number,
    j.alt_phone_number,
    j.dob,
    j.notes,
    j.email,
    j.last_update,
    j.new_title,
    j.new_first_name,
    j.new_last_name,
    j.new_address_1 as new_address,
    j.new_address_2 as new_address2,
    j.new_address_3 as new_address3,
    j.new_address_4 as new_address4,
    j.new_address_5 as new_address5,
    j.new_postcode as new_zip,
    j.date_received,
    j.processing_status,
    j.new_phone_number,
    j.new_alt_phone_number,
    j.new_dob,
    j.new_email,
    j.thirdparty_fname,
    j.thirdparty_lname,
    j.thirdparty_reason,
    j.thirdparty_other_reason,
    j.main_phone,
    j.other_phone,
    j.email_address,
    j.relationship,
    j.residency,
    j.residency_detail,
    j.mental_health_act,
    j.mental_health_act_details,
    j.bail,
    j.bail_details,
    j.convictions,
    j.convictions_details,
    j.deferral_reason,
    j.deferral_date,
    j.reasonable_adjustments_arrangements,
    j.excusal_reason,
    j.processing_complete,
    j.completed_at,
    j.version,
    j.juror_email_details,
    j.juror_phone_details,
    j.staff_login,
    j.staff_assignment_date,
    j.urgent,
    j.super_urgent,
    j.welsh,
    j.reply_type,
        CASE
            WHEN wl.loc_code IS NULL THEN false
            ELSE true
        END AS welsh_court
   FROM juror_details_cte j
     JOIN juror_mod.court_location c ON j.loc_code = c.loc_code
     LEFT JOIN juror_mod.welsh_court_location wl ON c.loc_code = wl.loc_code
 where row_no = 1;

 -- update juror_digital_response view to reference new address field names
 CREATE OR REPLACE VIEW juror_mod.juror_digital_response
 AS SELECT jr.juror_number,
     jr.date_received,
     jr.title,
     jr.first_name,
     jr.last_name,
     jr.address_line_1,
     jr.address_line_2,
     jr.address_line_3,
     jr.address_line_4,
     jr.address_line_5,
     jr.postcode,
     jr.processing_status,
     jr.date_of_birth,
     jr.phone_number,
     jr.alt_phone_number,
     jr.email,
     jr.residency,
     jr.residency_detail,
     jr.mental_health_act,
     jr.mental_health_act_details,
     jr.bail,
     jr.bail_details,
     jr.convictions,
     jr.convictions_details,
     jr.deferral_reason,
     jr.deferral_date,
     jr.reasonable_adjustments_arrangements,
     jr.excusal_reason,
     jr.processing_complete,
     jr.version,
     jr.thirdparty_fname,
     jr.thirdparty_lname,
     jr.relationship,
     jr.main_phone,
     jr.other_phone,
     jr.email_address,
     jr.thirdparty_reason,
     jr.thirdparty_other_reason,
     jr.juror_phone_details,
     jr.juror_email_details,
     jr.staff_login,
     jr.staff_assignment_date,
     jr.urgent,
     jr.super_urgent,
     jr.completed_at,
     jr.welsh
    FROM juror_mod.juror_response jr
   WHERE lower(jr.reply_type) = 'digital';

-- update juror_digital_response view to reference new address field names
CREATE OR REPLACE VIEW juror_mod.juror_paper_response
AS SELECT jr.juror_number,
   jr.date_received,
   jr.title,
   jr.first_name,
   jr.last_name,
   jr.address_line_1,
   jr.address_line_2,
   jr.address_line_3,
   jr.address_line_4,
   jr.address_line_5,
   jr.postcode,
   jr.processing_status,
   jr.date_of_birth,
   jr.phone_number,
   jr.alt_phone_number,
   jr.email,
   jr.residency,
   jr.mental_health_act,
   jr.mental_health_capacity,
   jr.bail,
   jr.convictions,
   jr.reasonable_adjustments_arrangements,
   jr.relationship,
   jr.thirdparty_reason,
   jr.deferral AS excusal,
   jr.signed,
   jr.staff_login,
   jr.urgent,
   jr.super_urgent,
   jr.processing_complete,
   jr.completed_at,
   jr.welsh
  FROM juror_mod.juror_response jr
 WHERE lower(jr.reply_type) = 'paper';