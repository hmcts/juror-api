-- Unmodified views added back at end of class
DROP VIEW IF EXISTS juror_mod.juror_paper_response;
DROP VIEW IF EXISTS juror_mod.juror_digital_response;
DROP VIEW IF EXISTS juror_mod.mod_juror_detail;
DROP VIEW IF EXISTS juror_mod.low_level_financial_audit_details_including_approved_amounts;
DROP VIEW IF EXISTS juror_mod.low_level_financial_audit_details;

alter table juror_mod.users
    alter column username type varchar(30);

alter table juror_mod.users_audit
    alter column username type varchar(30);

alter table juror_mod.user_roles
    alter column username type varchar(30);

alter table juror_mod.user_roles_audit
    alter column username type varchar(30);

alter table juror_mod.user_courts
    alter column username type varchar(30);

alter table juror_mod.user_courts_audit
    alter column username type varchar(30);

alter table juror_mod.juror_response
    alter column staff_login type varchar(30);

alter table juror_mod.juror_response_aud
    alter column login type varchar(30);

alter table juror_mod.juror_history
    alter column user_id type varchar(30);

alter table juror_mod.juror_pool
    alter column user_edtq type varchar(30);

alter table juror_mod.juror
    alter column user_edtq type varchar(30);

alter table juror_mod.financial_audit_details
    alter column created_by type varchar(30);

alter table juror_mod.message
    alter column username type varchar(30);

alter table juror_mod.contact_log
    alter column user_id type varchar(30);

alter table juror_mod.user_juror_response_audit
    alter column assigned_by type varchar(30),
    alter column assigned_to type varchar(30);

alter table juror_mod.pool_history
    alter column user_id type varchar(30);

alter table juror_mod.pool_history
    alter column user_id type varchar(30);

alter table juror_mod.rev_info
    alter column changed_by type varchar(30);

-- Unmodified views being added back

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
          jr.processing_complete,
          jr.completed_at,
          jr.welsh
   FROM juror_mod.juror_response jr
   WHERE lower(jr.reply_type::text) = 'paper'::text;

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
          jr.completed_at,
          jr.welsh
   FROM juror_mod.juror_response jr
   WHERE lower(jr.reply_type::text) = 'digital'::text;

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

CREATE OR REPLACE VIEW juror_mod.low_level_financial_audit_details
AS
select a4.*,
       a4.total_due - a4.total_paid as total_outstanding
from (select a3.*,
             a3.total_travel_due - a3.total_travel_paid                 as total_travel_outstanding,
             a3.total_financial_loss_due - a3.total_financial_loss_paid as total_financial_loss_outstanding,
             a3.total_subsistence_due - a3.total_subsistence_paid       as total_subsistence_outstanding,
             (case
                  when (a3.total_smart_card_paid - a3.total_smart_card_due) >= 0
                      then a3.total_smart_card_paid - a3.total_smart_card_due
                  else a3.total_smart_card_due
                 end)                                                   as total_smartcard_outstanding,
             (a3.total_travel_due + a3.total_financial_loss_due + a3.total_subsistence_due) -
             a3.total_smart_card_due                                    as total_due,
             (a3.total_travel_paid + a3.total_financial_loss_paid + a3.total_subsistence_paid) -
             a3.total_smart_card_paid                                   as total_paid

      from (select a2.*,
                   a2.public_transport_total_due +
                   a2.hired_vehicle_total_due +
                   a2.motorcycle_total_due +
                   a2.car_total_due +
                   a2.pedal_cycle_total_due +
                   a2.parking_total_due  AS total_travel_due,

                   a2.public_transport_total_paid +
                   a2.hired_vehicle_total_paid +
                   a2.motorcycle_total_paid +
                   a2.car_total_paid +
                   a2.pedal_cycle_total_paid +
                   a2.parking_total_paid AS total_travel_paid,

                   a2.loss_of_earnings_due +
                   a2.childcare_total_due +
                   a2.misc_total_due     AS total_financial_loss_due,

                   a2.loss_of_earnings_paid +
                   a2.childcare_total_paid +
                   a2.misc_total_paid    AS total_financial_loss_paid,

                   a2.subsistence_due    AS total_subsistence_due,
                   a2.subsistence_paid   AS total_subsistence_paid,
                   a2.smart_card_due     AS total_smart_card_due,
                   a2.smart_card_paid    AS total_smart_card_paid

            from (select fad.*,
                         fad.created_on::timestamp::date                      as created_on_date,
                         fada.last_approved_faudit,
                         aa.attendance_date,
                         aa.pool_number,
                         aa.trial_number,
                         aa.appearance_stage,
                         aa.attendance_type,
                         aa.is_draft_expense,
                         aa.pay_cash,
                         aa.f_audit,
                         aa.attendance_audit_number,

                         COALESCE(aa.public_transport_total_due, 0::numeric)  as public_transport_total_due,
                         COALESCE(aa.hired_vehicle_total_due, 0::numeric)     as hired_vehicle_total_due,
                         COALESCE(aa.motorcycle_total_due, 0::numeric)        as motorcycle_total_due,
                         COALESCE(aa.car_total_due, 0::numeric)               as car_total_due,
                         COALESCE(aa.pedal_cycle_total_due, 0::numeric)       as pedal_cycle_total_due,
                         COALESCE(aa.parking_total_due, 0::numeric)           as parking_total_due,
                         COALESCE(aa.loss_of_earnings_due, 0::numeric)        as loss_of_earnings_due,
                         COALESCE(aa.childcare_total_due, 0::numeric)         as childcare_total_due,
                         COALESCE(aa.misc_total_due, 0::numeric)              as misc_total_due,
                         COALESCE(aa.subsistence_due, 0::numeric)             as subsistence_due,
                         COALESCE(aa.smart_card_due, 0::numeric)              as smart_card_due,

                         COALESCE(aa.public_transport_total_paid, 0::numeric) as public_transport_total_paid,
                         COALESCE(aa.hired_vehicle_total_paid, 0::numeric)    as hired_vehicle_total_paid,
                         COALESCE(aa.motorcycle_total_paid, 0::numeric)       as motorcycle_total_paid,
                         COALESCE(aa.car_total_paid, 0::numeric)              as car_total_paid,
                         COALESCE(aa.pedal_cycle_total_paid, 0::numeric)      as pedal_cycle_total_paid,
                         COALESCE(aa.parking_total_paid, 0::numeric)          as parking_total_paid,
                         COALESCE(aa.loss_of_earnings_paid, 0::numeric)       as loss_of_earnings_paid,
                         COALESCE(aa.childcare_total_paid, 0::numeric)        as childcare_total_paid,
                         COALESCE(aa.misc_total_paid, 0::numeric)             as misc_total_paid,
                         COALESCE(aa.subsistence_paid, 0::numeric)            as subsistence_paid,
                         COALESCE(aa.smart_card_paid, 0::numeric)             as smart_card_paid

                  from juror_mod.financial_audit_details fad
                           join juror_mod.financial_audit_details_appearances fada
                                on fada.financial_audit_id = fad.id and fada.loc_code = fad.loc_code
                           join juror_mod.appearance_audit aa
                                on aa.loc_code = fada.loc_code and aa.attendance_date = fada.attendance_date and
                                   aa.juror_number = fad.juror_number
                                    and aa."version" = fada.appearance_version) a2) a3) a4;
;

CREATE OR REPLACE VIEW juror_mod.low_level_financial_audit_details_including_approved_amounts
AS SELECT llfad2.public_transport_total_approved,
          llfad2.hired_vehicle_total_approved,
          llfad2.motorcycle_total_approved,
          llfad2.car_total_approved,
          llfad2.pedal_cycle_total_approved,
          llfad2.parking_total_approved,
          llfad2.loss_of_earnings_approved,
          llfad2.childcare_total_approved,
          llfad2.misc_total_approved,
          llfad2.subsistence_approved,
          llfad2.smart_card_approved,
          llfad2.id,
          llfad2.juror_revision,
          llfad2.court_location_revision,
          llfad2.type,
          llfad2.created_by,
          llfad2.created_on,
          llfad2.juror_number,
          llfad2.loc_code,
          llfad2.created_on_date,
          llfad2.last_approved_faudit,
          llfad2.attendance_date,
          llfad2.pool_number,
          llfad2.trial_number,
          llfad2.appearance_stage,
          llfad2.attendance_type,
          llfad2.is_draft_expense,
          llfad2.pay_cash,
          llfad2.f_audit,
          llfad2.attendance_audit_number,
          llfad2.public_transport_total_due,
          llfad2.hired_vehicle_total_due,
          llfad2.motorcycle_total_due,
          llfad2.car_total_due,
          llfad2.pedal_cycle_total_due,
          llfad2.parking_total_due,
          llfad2.loss_of_earnings_due,
          llfad2.childcare_total_due,
          llfad2.misc_total_due,
          llfad2.subsistence_due,
          llfad2.smart_card_due,
          llfad2.public_transport_total_paid,
          llfad2.hired_vehicle_total_paid,
          llfad2.motorcycle_total_paid,
          llfad2.car_total_paid,
          llfad2.pedal_cycle_total_paid,
          llfad2.parking_total_paid,
          llfad2.loss_of_earnings_paid,
          llfad2.childcare_total_paid,
          llfad2.misc_total_paid,
          llfad2.subsistence_paid,
          llfad2.smart_card_paid,
          llfad2.total_travel_due,
          llfad2.total_travel_paid,
          llfad2.total_financial_loss_due,
          llfad2.total_financial_loss_paid,
          llfad2.total_subsistence_due,
          llfad2.total_subsistence_paid,
          llfad2.total_smart_card_due,
          llfad2.total_smart_card_paid,
          llfad2.total_travel_outstanding,
          llfad2.total_financial_loss_outstanding,
          llfad2.total_subsistence_outstanding,
          llfad2.total_smartcard_outstanding,
          llfad2.total_due,
          llfad2.total_paid,
          llfad2.total_outstanding,
          llfad2.public_transport_total_approved + llfad2.hired_vehicle_total_approved + llfad2.motorcycle_total_approved + llfad2.car_total_approved + llfad2.pedal_cycle_total_approved + llfad2.parking_total_approved + llfad2.loss_of_earnings_approved + llfad2.childcare_total_approved + llfad2.misc_total_approved + llfad2.subsistence_approved + llfad2.smart_card_approved AS total_approved,
          llfad2.public_transport_total_approved + llfad2.hired_vehicle_total_approved + llfad2.motorcycle_total_approved + llfad2.car_total_approved + llfad2.pedal_cycle_total_approved + llfad2.parking_total_approved AS total_travel_approved,
          llfad2.loss_of_earnings_approved + llfad2.childcare_total_approved + llfad2.misc_total_approved AS total_financial_loss_approved,
          llfad2.subsistence_approved AS total_subsistence_approved,
          llfad2.smart_card_approved AS total_smartcard_approved
   FROM ( SELECT llfad.public_transport_total_paid - COALESCE(lastapprovedllfad.public_transport_total_paid, 0::numeric) AS public_transport_total_approved,
                 llfad.hired_vehicle_total_paid - COALESCE(lastapprovedllfad.hired_vehicle_total_paid, 0::numeric) AS hired_vehicle_total_approved,
                 llfad.motorcycle_total_paid - COALESCE(lastapprovedllfad.motorcycle_total_paid, 0::numeric) AS motorcycle_total_approved,
                 llfad.car_total_paid - COALESCE(lastapprovedllfad.car_total_paid, 0::numeric) AS car_total_approved,
                 llfad.pedal_cycle_total_paid - COALESCE(lastapprovedllfad.pedal_cycle_total_paid, 0::numeric) AS pedal_cycle_total_approved,
                 llfad.parking_total_paid - COALESCE(lastapprovedllfad.parking_total_paid, 0::numeric) AS parking_total_approved,
                 llfad.loss_of_earnings_paid - COALESCE(lastapprovedllfad.loss_of_earnings_paid, 0::numeric) AS loss_of_earnings_approved,
                 llfad.childcare_total_paid - COALESCE(lastapprovedllfad.childcare_total_paid, 0::numeric) AS childcare_total_approved,
                 llfad.misc_total_paid - COALESCE(lastapprovedllfad.misc_total_paid, 0::numeric) AS misc_total_approved,
                 llfad.subsistence_paid - COALESCE(lastapprovedllfad.subsistence_paid, 0::numeric) AS subsistence_approved,
                 CASE
                     WHEN llfad.type::text = ANY (ARRAY['REAPPROVED_CASH'::character varying, 'REAPPROVED_BACS'::character varying]::text[])
                         THEN COALESCE(lastapprovedllfad.smart_card_paid, 0::numeric) - llfad.smart_card_paid
                     ELSE -(llfad.smart_card_paid - COALESCE(lastapprovedllfad.smart_card_paid, 0::numeric))
                     END AS smart_card_approved,
                 llfad.id,
                 llfad.juror_revision,
                 llfad.court_location_revision,
                 llfad.type,
                 llfad.created_by,
                 llfad.created_on,
                 llfad.juror_number,
                 llfad.loc_code,
                 llfad.created_on_date,
                 llfad.last_approved_faudit,
                 llfad.attendance_date,
                 llfad.pool_number,
                 llfad.trial_number,
                 llfad.appearance_stage,
                 llfad.attendance_type,
                 llfad.is_draft_expense,
                 llfad.pay_cash,
                 llfad.f_audit,
                 llfad.attendance_audit_number,
                 llfad.public_transport_total_due,
                 llfad.hired_vehicle_total_due,
                 llfad.motorcycle_total_due,
                 llfad.car_total_due,
                 llfad.pedal_cycle_total_due,
                 llfad.parking_total_due,
                 llfad.loss_of_earnings_due,
                 llfad.childcare_total_due,
                 llfad.misc_total_due,
                 llfad.subsistence_due,
                 llfad.smart_card_due,
                 llfad.public_transport_total_paid,
                 llfad.hired_vehicle_total_paid,
                 llfad.motorcycle_total_paid,
                 llfad.car_total_paid,
                 llfad.pedal_cycle_total_paid,
                 llfad.parking_total_paid,
                 llfad.loss_of_earnings_paid,
                 llfad.childcare_total_paid,
                 llfad.misc_total_paid,
                 llfad.subsistence_paid,
                 llfad.smart_card_paid,
                 llfad.total_travel_due,
                 llfad.total_travel_paid,
                 llfad.total_financial_loss_due,
                 llfad.total_financial_loss_paid,
                 llfad.total_subsistence_due,
                 llfad.total_subsistence_paid,
                 llfad.total_smart_card_due,
                 llfad.total_smart_card_paid,
                 llfad.total_travel_outstanding,
                 llfad.total_financial_loss_outstanding,
                 llfad.total_subsistence_outstanding,
                 llfad.total_smartcard_outstanding,
                 llfad.total_due,
                 llfad.total_paid,
                 llfad.total_outstanding
          FROM juror_mod.low_level_financial_audit_details llfad
                   LEFT JOIN juror_mod.low_level_financial_audit_details lastapprovedllfad ON lastapprovedllfad.id = llfad.last_approved_faudit AND lastapprovedllfad.loc_code::text = llfad.loc_code::text AND lastapprovedllfad.attendance_date = llfad.attendance_date AND (llfad.type::text = ANY (ARRAY['REAPPROVED_BACS'::character varying, 'REAPPROVED_CASH'::character varying]::text[]))) llfad2;