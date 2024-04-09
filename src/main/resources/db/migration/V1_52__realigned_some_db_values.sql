DROP VIEW juror_mod.court_excusal_granted;
DROP VIEW juror_mod.court_excusal_refused;


DROP VIEW juror_mod.court_deferral_granted;
DROP VIEW juror_mod.court_deferral_denied;

DROP VIEW juror_mod.court_postponement;

ALTER TABLE juror_mod.juror
ALTER COLUMN excusal_code SET DATA TYPE VARCHAR(2);


ALTER TABLE juror_mod.juror_pool
    ALTER COLUMN deferral_code SET DATA TYPE VARCHAR(2);
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

create or replace view juror_mod.court_deferral_denied as
select
    jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    jh.date_created as refusal_date,
    jh.other_information,
    jh_lett.date_created as date_printed,
    jp.is_active,
    row_number() over (partition by j.juror_number
        order by
            jh.date_created desc) as row_no
from
    juror_mod.juror_pool jp
        join juror_mod.juror j on
        j.juror_number = jp.juror_number
        join juror_mod.t_juror_status js on
        js.status = jp.status
        join juror_mod.juror_history jh on
        jh.juror_number = j.juror_number
            and jh.history_code = 'PDEF'
            and lower(jh.other_information) like 'deferral denied%'
            and jh.date_created >= j.bureau_transfer_date
        left join juror_mod.juror_history jh_lett on
        jh_lett.juror_number = j.juror_number
            and jh_lett.history_code = 'RDDL'
            and jh_lett.date_created >= jh.date_created
where
    upper(j.acc_exc) = 'Z'
  and jp.owner <> '400'
  and jp.is_active = true;

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