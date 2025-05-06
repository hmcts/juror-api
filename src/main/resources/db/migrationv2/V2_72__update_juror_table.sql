-- drop views dependent on the juror lastname
drop view if exists juror_mod.court_withdrawal;
drop view if exists juror_mod.court_excusal_granted;
drop view if exists juror_mod.court_deferral_denied;
drop view if exists juror_mod.court_deferral_granted;
drop view if exists juror_mod.court_excusal_refused;
drop view if exists juror_mod.court_postponement;
drop view if exists juror_mod.reports_juror_payments_summary;
drop view if exists juror_mod.show_cause;
drop view if exists juror_mod.failed_to_attend;
drop view if exists juror_mod.mod_juror_detail;
drop view if exists juror_mod.require_pnc_check_view;

-- update the juror table
alter table juror_mod.juror
ALTER COLUMN last_name TYPE VARCHAR(25);

-- update the voters table
alter table juror_mod.voters
ALTER COLUMN lname TYPE VARCHAR(25);

-- Recreate all the dropped views

-- juror_mod.court_withdrawal source

CREATE OR REPLACE VIEW juror_mod.court_withdrawal
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    j.date_disq,
    d.disq_code,
    jh.date_created AS date_printed,
    jp.is_active,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY j.date_disq DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_disq_code d ON d.disq_code::text = j.disq_code::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = jp.juror_number::text AND jh.pool_number::text = jp.pool_number::text AND jh.history_code::text = 'RDIS'::text AND jh.date_created::date > j.bureau_transfer_date
  WHERE jp.status = 6 AND jp.owner::text <> '400'::text;

-- juror_mod.court_excusal_granted source

CREATE OR REPLACE VIEW juror_mod.court_excusal_granted
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    j.date_excused,
    d.description AS excusal_reason,
    jh.date_created AS date_printed,
    jp.is_active,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY j.date_excused DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_exc_code d ON d.exc_code::text = j.excusal_code::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = jp.juror_number::text AND jh.pool_number::text = jp.pool_number::text AND jh.history_code::text = 'REXC'::text AND jh.date_created::date > j.bureau_transfer_date
  WHERE jp.status = 5 AND jp.owner::text <> '400'::text;

-- juror_mod.court_deferral_denied source

CREATE OR REPLACE VIEW juror_mod.court_deferral_denied
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    jh.date_created AS refusal_date,
    jh.other_information,
    jh_lett.date_created AS date_printed,
    jp.is_active,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY jh.date_created DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     JOIN juror_mod.juror_history jh ON jh.juror_number::text = j.juror_number::text AND jh.history_code::text = 'PDEF'::text AND lower(jh.other_information) ~~ 'deferral denied%'::text AND jh.date_created >= j.bureau_transfer_date
     LEFT JOIN juror_mod.juror_history jh_lett ON jh_lett.juror_number::text = j.juror_number::text AND jh_lett.history_code::text = 'RDDL'::text AND jh_lett.date_created >= jh.date_created
  WHERE upper(j.acc_exc::text) = 'Z'::text AND jp.owner::text <> '400'::text AND jp.is_active = true;


-- juror_mod.court_deferral_granted source

CREATE OR REPLACE VIEW juror_mod.court_deferral_granted
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    jp.def_date,
    d.description AS deferral_reason,
    jh.date_created AS date_printed,
    jp.is_active,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY jp.def_date DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_exc_code d ON d.exc_code::text = jp.deferral_code::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = jp.juror_number::text AND jh.pool_number::text = jp.pool_number::text AND jh.history_code::text = 'RDEF'::text AND jh.other_info_date = jp.def_date AND jh.date_created > j.bureau_transfer_date
  WHERE jp.status = 7 AND d.exc_code::text <> 'P'::text;

-- juror_mod.court_excusal_refused source

CREATE OR REPLACE VIEW juror_mod.court_excusal_refused
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    j.date_excused,
    d.description AS excusal_reason,
    jh.date_created AS date_printed,
    jp.is_active,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY j.date_excused DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_exc_code d ON d.exc_code::text = j.excusal_code::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     JOIN juror_mod.juror_history jh ON jh.juror_number::text = j.juror_number::text AND jh.history_code::text = 'PEXC'::text AND lower(jh.other_information) ~~ 'refuse excuse%'::text AND jh.date_created > j.bureau_transfer_date
     LEFT JOIN juror_mod.juror_history jh_lett ON jh.juror_number::text = jp.juror_number::text AND jh_lett.pool_number::text = jp.pool_number::text AND jh_lett.history_code::text = 'REDL'::text AND jh_lett.date_created = j.date_excused AND jh_lett.date_created > j.bureau_transfer_date
  WHERE jp.owner::text <> '400'::text AND jp.is_active = true AND j.acc_exc::text = 'Y'::text;


-- juror_mod.court_postponement source

CREATE OR REPLACE VIEW juror_mod.court_postponement
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    jp.def_date,
    ec.description AS deferral_reason,
    jh.date_created AS date_printed,
    jp.is_active,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY jp.def_date DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_exc_code ec ON ec.exc_code::text = jp.deferral_code::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = jp.juror_number::text AND jh.pool_number::text = jp.pool_number::text AND jh.history_code::text = 'RPST'::text AND jh.other_info_date = jp.def_date AND jh.date_created > j.bureau_transfer_date
  WHERE jp.status = 7 AND ec.exc_code::text = 'P'::text AND jp.owner::text <> '400'::text;

   -- juror_mod.reports_juror_payments_summary source

   CREATE OR REPLACE VIEW juror_mod.reports_juror_payments_summary
   AS WITH payment_audits AS (
            SELECT fad_1.juror_number,
               fad_1.loc_code,
               fad_1.id AS latest_payment_f_audit_id
              FROM juror_mod.financial_audit_details fad_1
             WHERE fad_1.type::text = ANY (ARRAY['APPROVED_BACS'::character varying::text, 'APPROVED_CASH'::character varying::text, 'REAPPROVED_CASH'::character varying::text, 'REAPPROVED_BACS'::character varying::text])
           ), attendance_dates AS (
            SELECT pa.juror_number,
               fada.loc_code,
               max(fada.financial_audit_id) AS latest_payment_f_audit_id,
               fada.attendance_date
              FROM juror_mod.financial_audit_details_appearances fada
                JOIN payment_audits pa ON pa.loc_code::text = fada.loc_code::text AND pa.latest_payment_f_audit_id = fada.financial_audit_id
             GROUP BY pa.juror_number, fada.loc_code, fada.attendance_date
             ORDER BY pa.juror_number
           )
    SELECT a.trial_number,
       a.non_attendance,
       a.loc_code,
       a.attendance_date,
       a.juror_number,
       j.first_name,
       j.last_name,
       a.pool_number,
       ad.latest_payment_f_audit_id,
       a.time_in AS checked_in,
       a.time_out AS checked_out,
       a.time_out - a.time_in AS hours_attended,
       a.attendance_audit_number AS attendance_audit,
       fad.created_on AS payment_date,
       COALESCE(a.public_transport_total_due, 0::numeric) + COALESCE(a.hired_vehicle_total_due, 0::numeric) + COALESCE(a.motorcycle_total_due, 0::numeric) + COALESCE(a.car_total_due, 0::numeric) + COALESCE(a.pedal_cycle_total_due, 0::numeric) + COALESCE(a.parking_total_due, 0::numeric) AS total_travel_due,
       COALESCE(a.loss_of_earnings_due, 0::numeric) + COALESCE(a.childcare_total_due, 0::numeric) + COALESCE(a.misc_total_due, 0::numeric) AS total_financial_loss_due,
       COALESCE(a.subsistence_due, 0::numeric) AS subsistence_due,
       COALESCE(a.smart_card_due, 0::numeric) AS smart_card_due,
       COALESCE(a.public_transport_total_due, 0::numeric) + COALESCE(a.hired_vehicle_total_due, 0::numeric) + COALESCE(a.motorcycle_total_due, 0::numeric) + COALESCE(a.car_total_due, 0::numeric) + COALESCE(a.pedal_cycle_total_due, 0::numeric) + COALESCE(a.parking_total_due, 0::numeric) + COALESCE(a.loss_of_earnings_due, 0::numeric) + COALESCE(a.childcare_total_due, 0::numeric) + COALESCE(a.misc_total_due, 0::numeric) + COALESCE(a.subsistence_due, 0::numeric) - COALESCE(a.smart_card_due, 0::numeric) AS total_due,
       COALESCE(a.public_transport_total_paid, 0::numeric) + COALESCE(a.hired_vehicle_total_paid, 0::numeric) + COALESCE(a.motorcycle_total_paid, 0::numeric) + COALESCE(a.car_total_paid, 0::numeric) + COALESCE(a.pedal_cycle_total_paid, 0::numeric) + COALESCE(a.parking_total_paid, 0::numeric) + COALESCE(a.loss_of_earnings_paid, 0::numeric) + COALESCE(a.childcare_total_paid, 0::numeric) + COALESCE(a.misc_total_paid, 0::numeric) + COALESCE(a.subsistence_paid, 0::numeric) - COALESCE(a.smart_card_paid, 0::numeric) AS total_paid
      FROM juror_mod.appearance a
        JOIN juror_mod.juror j ON a.juror_number::text = j.juror_number::text
        LEFT JOIN attendance_dates ad ON ad.juror_number::text = a.juror_number::text AND ad.attendance_date = a.attendance_date AND ad.loc_code::text = a.loc_code::text
        LEFT JOIN juror_mod.financial_audit_details fad ON fad.juror_number::text = j.juror_number::text AND fad.loc_code::text = a.loc_code::text AND fad.id = ad.latest_payment_f_audit_id;

-- juror_mod.show_cause source

CREATE OR REPLACE VIEW juror_mod.show_cause
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    j.date_disq,
    jh.date_created AS date_printed,
    jp.is_active,
    a.attendance_date,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY j.date_disq DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     JOIN juror_mod.appearance a ON a.juror_number::text = jp.juror_number::text
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = jp.juror_number::text AND jh.pool_number::text = jp.pool_number::text AND jh.history_code::text = 'RSHC'::text
  WHERE a.no_show = true AND a.attendance_type::text = 'ABSENT'::text AND jp.owner::text <> '400'::text;

-- juror_mod.failed_to_attend source

CREATE OR REPLACE VIEW juror_mod.failed_to_attend
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    j.date_disq,
    jh.date_created AS date_printed,
    jp.is_active,
    a.attendance_date,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY j.date_disq DESC) AS row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     JOIN juror_mod.appearance a ON a.juror_number::text = jp.juror_number::text
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = jp.juror_number::text AND jh.pool_number::text = jp.pool_number::text AND jh.history_code::text = 'RFTA'::text
  WHERE a.no_show = true AND a.attendance_type::text = 'ABSENT'::text AND jp.owner::text <> '400'::text;

-- juror_mod.mod_juror_detail source

CREATE OR REPLACE VIEW juror_mod.mod_juror_detail
AS WITH juror_details_cte AS (
         SELECT j_1.juror_number,
            COALESCE(s.pool_no, jp.pool_number) AS pool_no,
            COALESCE(s.service_start_date, p.return_date) AS ret_date,
            COALESCE(s.loc_code, p.loc_code) AS loc_code,
            j_1.title,
            j_1.first_name,
            j_1.last_name,
            j_1.address_line_1,
            j_1.address_line_2,
            j_1.address_line_3,
            j_1.address_line_4,
            j_1.address_line_5,
            j_1.postcode,
            jp.next_date,
            jp.status,
            jp.owner,
            j_1.h_phone AS phone_number,
            j_1.m_phone AS alt_phone_number,
            j_1.dob,
            j_1.notes,
            j_1.h_email AS email,
            j_1.last_update,
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
            r.welsh,
            r.reply_type,
            jp.is_active,
            row_number() OVER (PARTITION BY j_1.juror_number ORDER BY p.return_date DESC) AS row_no
           FROM juror_mod.juror j_1
             LEFT JOIN juror_mod.juror_pool jp ON j_1.juror_number::text = jp.juror_number::text
             LEFT JOIN juror_mod.pool p ON jp.pool_number::text = p.pool_no::text
             LEFT JOIN juror_mod.juror_response r ON r.juror_number::text = j_1.juror_number::text
             LEFT JOIN juror_mod.summons_snapshot s ON r.juror_number::text = s.juror_number::text
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
    j.address_line_1 AS address,
    j.address_line_2 AS address2,
    j.address_line_3 AS address3,
    j.address_line_4 AS address4,
    j.address_line_5 AS address5,
    j.postcode AS zip,
    j.next_date,
    j.status,
    j.owner,
    j.phone_number,
    j.alt_phone_number,
    j.dob,
    j.notes,
    j.email,
    j.last_update,
    j.new_title,
    j.new_first_name,
    j.new_last_name,
    j.new_address_1 AS new_address,
    j.new_address_2 AS new_address2,
    j.new_address_3 AS new_address3,
    j.new_address_4 AS new_address4,
    j.new_address_5 AS new_address5,
    j.new_postcode AS new_zip,
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
    j.welsh,
    j.reply_type,
    j.is_active,
        CASE
            WHEN wl.loc_code IS NULL THEN false
            ELSE true
        END AS welsh_court
   FROM juror_details_cte j
     JOIN juror_mod.court_location c ON j.loc_code::text = c.loc_code::text
     LEFT JOIN juror_mod.welsh_court_location wl ON c.loc_code::text = wl.loc_code::text
  WHERE j.row_no = 1;

-- juror_mod.require_pnc_check_view source

CREATE OR REPLACE VIEW juror_mod.require_pnc_check_view
AS SELECT j.police_check,
    j.juror_number,
    regexp_replace(replace(replace(j.first_name::text, ' -'::text, '-'::text), '- '::text, '-'::text), '\s.*'::text, ''::text) AS first_name,
    NULLIF(regexp_replace(regexp_replace(j.first_name::text, '(.*)-[^-]*'::text, ''::text), '.*?\s'::text, ''::text), j.first_name::text) AS middle_name,
    regexp_replace(j.last_name::text, '\s'::text, ''::text, 'g'::text) AS last_name,
    j.dob AS date_of_birth,
    upper(regexp_replace(j.postcode::text, '\s+'::text, ''::text)) AS post_code
   FROM juror_mod.juror j
     JOIN juror_mod.juror_pool jp ON jp.juror_number::text = j.juror_number::text
  WHERE jp.status = 2 AND (j.police_check IS NULL OR (j.police_check::text <> ALL (ARRAY['UNCHECKED_MAX_RETRIES_EXCEEDED'::character varying::text, 'ELIGIBLE'::character varying::text, 'INELIGIBLE'::character varying::text]))) AND jp.owner::text = '400'::text AND jp.is_active = true AND upper(regexp_replace(j.postcode::text, '\s+'::text, ''::text)) ~ '^[A-Z0-9]{5,8}$'::text;
