DROP VIEW juror_mod.mod_juror_detail;
DROP VIEW juror_mod.summons_snapshot;

alter table juror_mod.court_location
  ALTER COLUMN loc_court_name TYPE character varying(40);

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

CREATE OR REPLACE VIEW juror_mod.summons_snapshot
AS WITH original_summons_cte AS (
    SELECT jh.juror_number,
           jh.pool_number AS pool_no,
           jh.date_created,
           row_number() OVER (PARTITION BY jh.juror_number ORDER BY jh.date_created) AS row_no
    FROM juror_mod.juror_history jh
    WHERE jh.history_code::text = 'RSUM'::text
)
   SELECT os.juror_number,
          os.pool_no,
          p.return_date AS service_start_date,
          p.loc_code,
          cl.loc_name AS location_name,
          cl.loc_court_name AS court_name,
          os.date_created
   FROM original_summons_cte os
            JOIN juror_mod.pool p ON p.pool_no::text = os.pool_no::text
            JOIN juror_mod.court_location cl ON p.loc_code::text = cl.loc_code::text
   WHERE os.row_no = 1;

update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT SOUTHEND' where loc_code = '772';
update juror_mod.court_location set loc_court_name = 'PRESTON COMBINED COURT CENTRE' where loc_code = '448';
update juror_mod.court_location set loc_court_name = 'WARWICK sitting at Leamington Spa' where loc_code = '463';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT NEWPORT IOW' where loc_code = '478';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT READING' where loc_code = '449';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT DURHAM' where loc_code = '422';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BRADFORD' where loc_code = '402';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT SOUTHWARK' where loc_code = '471';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT LEWES' where loc_code = '431';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT DERBY' where loc_code = '419';
update juror_mod.court_location set loc_court_name = 'The Crown Court at Bournemouth' where loc_code = '406';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT HEREFORD' where loc_code = '762';
update juror_mod.court_location set loc_court_name = 'Winchester Combined Courts' where loc_code = '465';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT OXFORD' where loc_code = '445';
update juror_mod.court_location set loc_court_name = 'HAVERFORDWEST' where loc_code = '761';
update juror_mod.court_location set loc_court_name = 'INNER LONDON CROWN COURT' where loc_code = '440';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT MANCHESTER' where loc_code = '436';
update juror_mod.court_location set loc_court_name = 'SALISBURY CROWN COURT' where loc_code = '480';
update juror_mod.court_location set loc_court_name = 'Warwickshire Justice Centre' where loc_code = '000';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT SHREWSBURY' where loc_code = '452';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CHICHESTER' where loc_code = '416';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT HARROW' where loc_code = '468';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT TRURO' where loc_code = '477';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CAERNARFON' where loc_code = '755';
update juror_mod.court_location set loc_court_name = 'BURY ST EDMUNDS CROWN COURT' where loc_code = '754';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT LANCASTER' where loc_code = '768';
update juror_mod.court_location set loc_court_name = 'CARMARTHEN' where loc_code = '756';
update juror_mod.court_location set loc_court_name = 'Redditch County Court' where loc_code = '797';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT SWANSEA' where loc_code = '457';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT SWINDON' where loc_code = '458';
update juror_mod.court_location set loc_court_name = 'MIDDLESEX  GUILDHALL CROWN CRT' where loc_code = '464';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT NOTTINGHAM' where loc_code = '444';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT ST ALBANS' where loc_code = '450';
update juror_mod.court_location set loc_court_name = 'CIRENCESTER MAGISTRATES COURT' where loc_code = '795';
update juror_mod.court_location set loc_court_name = 'PORTSMOUTH COMBINED COURT' where loc_code = '447';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT WOOD GREEN' where loc_code = '469';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT GLOUCESTER' where loc_code = '424';
update juror_mod.court_location set loc_court_name = 'The Crown Court at Snaresbrook' where loc_code = '453';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT DOLGELLAU' where loc_code = '758';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BLACKFRIARS' where loc_code = '428';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT WOOLWICH' where loc_code = '472';
update juror_mod.court_location set loc_court_name = 'BARROW-IN-FURNESS CROWN COURT' where loc_code = '751';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT GUILDFORD' where loc_code = '474';
update juror_mod.court_location set loc_court_name = 'HOVE TRIAL CENTRE' where loc_code = '799';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT PLYMOUTH' where loc_code = '446';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT NORTHAMPTON' where loc_code = '442';
update juror_mod.court_location set loc_court_name = 'Bureau' where loc_code = '400';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT IPSWICH' where loc_code = '426';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BASILDON' where loc_code = '461';
update juror_mod.court_location set loc_court_name = 'KIDDERMINSTER' where loc_code = '798';
update juror_mod.court_location set loc_court_name = 'CROWN COURT AT STOKE-ON-TRENT' where loc_code = '456';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT MANCHESTER' where loc_code = '435';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT LEICESTER' where loc_code = '430';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT TAUNTON' where loc_code = '459';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BURNLEY' where loc_code = '409';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT DONCASTER' where loc_code = '420';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BRIGHTON ' where loc_code = '777';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT DORCHESTER' where loc_code = '407';
update juror_mod.court_location set loc_court_name = 'BIRMINGHAM CIVIL JUSTICE CENTR' where loc_code = '127';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CANTERBURY' where loc_code = '479';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT LUTON' where loc_code = '476';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT GRIMSBY' where loc_code = '425';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT TEESSIDE ' where loc_code = '460';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT WELSHPOOL' where loc_code = '774';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BRISTOL' where loc_code = '408';
update juror_mod.court_location set loc_court_name = 'Royal Courts Of Justice' where loc_code = '626';
update juror_mod.court_location set loc_court_name = 'NORWICH COMBINED COURT' where loc_code = '443';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT KNUTSFORD' where loc_code = '767';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT EXETER' where loc_code = '423';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT LINCOLN ' where loc_code = '432';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT WORCESTER' where loc_code = '466';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT COVENTRY' where loc_code = '417';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT STAFFORD' where loc_code = '455';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT KINGSTON' where loc_code = '427';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT LEEDS' where loc_code = '429';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT MOLD' where loc_code = '769';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CAMBRIDGE' where loc_code = '410';
update juror_mod.court_location set loc_court_name = 'BOLTON COMBINED COURT CENTRE' where loc_code = '470';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT WARRINGTON' where loc_code = '462';
update juror_mod.court_location set loc_court_name = 'KINGSTON-U-HULL' where loc_code = '403';
update juror_mod.court_location set loc_court_name = 'Huntingdon Law Courts' where loc_code = '796';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT KINGS LYNN' where loc_code = '765';
update juror_mod.court_location set loc_court_name = 'WOLVERHAMPTON COMBINED COURT' where loc_code = '421';
update juror_mod.court_location set loc_court_name = 'MERTHYR TYDFIL' where loc_code = '437';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CROYDON' where loc_code = '418';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CARLISLE' where loc_code = '412';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT ISLEWORTH' where loc_code = '475';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BIRMINGHAM' where loc_code = '404';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CHESTER' where loc_code = '415';
update juror_mod.court_location set loc_court_name = 'MAIDSTONE COMBINED COURT' where loc_code = '434';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT AYLESBURY ' where loc_code = '401';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CHELMSFORD' where loc_code = '414';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT PETERBOROUGH' where loc_code = '473';
update juror_mod.court_location set loc_court_name = 'NEWCASTLE UPON TYNE' where loc_code = '439';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT BARNSTAPLE' where loc_code = '750';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT CARDIFF' where loc_code = '411';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT YORK' where loc_code = '467';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT NEWPORT' where loc_code = '441';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT LIVERPOOL' where loc_code = '433';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT SOUTHAMPTON' where loc_code = '454';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT SHEFFIELD' where loc_code = '451';
update juror_mod.court_location set loc_court_name = 'THE CROWN COURT AT WIMBLEDON' where loc_code = '794';
update juror_mod.court_location set loc_court_name = 'The Central Criminal Court' where loc_code = '413';