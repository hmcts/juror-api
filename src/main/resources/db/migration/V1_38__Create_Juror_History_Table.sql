CREATE TABLE juror_mod.juror_history (
	id bigserial NOT NULL,
	juror_number varchar(9) NOT NULL,
	date_created timestamp NULL,
	history_code varchar(4) NOT NULL,
	user_id varchar(20) NOT NULL,
	other_information text NULL,
	pool_number varchar(9) NULL,
	CONSTRAINT juror_hist_pk PRIMARY KEY (id)
);

-- juror_mod.juror_history foreign keys
ALTER TABLE juror_mod.juror_history ADD CONSTRAINT juror_history_fk FOREIGN KEY (juror_number) REFERENCES juror_mod
.juror (juror_number);
ALTER TABLE juror_mod.juror_history ADD CONSTRAINT juror_history_hist_code_fk FOREIGN KEY (history_code) REFERENCES
juror_mod.t_history_code (history_code);

-- recreate the summons_snapshot view using the new history table
CREATE OR REPLACE VIEW juror_mod.summons_snapshot
AS WITH original_summons_cte AS (
         SELECT jh.juror_number,
            jh.pool_number AS pool_no,
            jh.date_created,
            row_number() OVER (PARTITION BY jh.juror_number ORDER BY jh.date_created) AS row_no
           FROM juror_mod.juror_history jh
          WHERE jh.history_code = 'RSUM'
        )
 SELECT os.juror_number,
    os.pool_no,
    p.return_date AS service_start_date,
    p.loc_code,
    cl.loc_name AS location_name,
    cl.loc_court_name AS court_name,
    os.date_created
   FROM original_summons_cte os
     JOIN juror_mod.pool p ON p.pool_no = os.pool_no
     JOIN juror_mod.court_location cl ON p.loc_code = cl.loc_code
  WHERE os.row_no = 1;


CREATE OR REPLACE VIEW juror_mod.mod_juror_detail
AS
WITH juror_details_cte AS (
         SELECT j.juror_number,
            COALESCE(s.pool_no, jp.pool_number) AS pool_no,
            COALESCE(s.service_start_date, jp.ret_date) AS ret_date,
            COALESCE(s.loc_code, p.loc_code) AS loc_code,
            j.title,
            j.first_name,
            j.last_name,
            j.address,
            j.address2,
            j.address3,
            j.address4,
            j.address5,
            j.address6,
            j.zip,
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
            r.address AS new_address,
            r.address2 AS new_address2,
            r.address3 AS new_address3,
            r.address4 AS new_address4,
            r.address5 AS new_address5,
            r.address6 AS new_address6,
            r.zip AS new_zip,
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
            r.special_needs_arrangements,
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
            row_number() over (partition by j.juror_number order by jp.ret_date desc) AS row_no
           FROM juror_mod.juror j
             LEFT JOIN juror_mod.juror_pool jp ON j.juror_number = jp.juror_number
             LEFT JOIN juror_mod.pool p ON jp.pool_number = p.pool_no
             LEFT JOIN juror_digital.juror_response r ON r.juror_number = j.juror_number
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
    j.address,
    j.address2,
    j.address3,
    j.address4,
    j.address5,
    j.address6,
    j.zip,
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
    j.new_address,
    j.new_address2,
    j.new_address3,
    j.new_address4,
    j.new_address5,
    j.new_address6,
    j.new_zip,
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
    j.special_needs_arrangements,
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
        CASE
            WHEN wl.loc_code IS NULL THEN false
            ELSE true
        END AS welsh_court
   FROM juror_details_cte j
     JOIN juror_mod.court_location c ON j.loc_code = c.loc_code
     LEFT JOIN juror_mod.welsh_court_location wl ON c.loc_code = wl.loc_code
 where row_no = 1;