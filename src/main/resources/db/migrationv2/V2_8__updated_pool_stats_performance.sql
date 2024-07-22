
CREATE OR REPLACE VIEW juror_mod.pool_stats
AS
SELECT jp.pool_number,
       count(*) FILTER (WHERE owner = '400')                                 AS total_summoned,
       count(*) FILTER (WHERE owner <> '400' and jp.is_active)               AS court_supply,
       count(*) FILTER (WHERE owner = '400' and jp.status = 2)               AS available,
       count(*) FILTER (WHERE owner = '400' and jp.status not in (1, 2, 11)) AS unavailable,
       count(*) FILTER (WHERE owner = '400' and jp.status in (1, 11))        AS unresolved
FROM juror_mod.juror_pool jp
         JOIN juror_mod.juror j ON jp.juror_number::text = j.juror_number::text
WHERE j.summons_file IS NULL
   OR j.summons_file::text <> 'Disq. on selection'::text
GROUP BY jp.pool_number;

-- This is significantly more performance then using the view and joining on the pool table
CREATE OR REPLACE VIEW juror_mod.pool_stats_with_pool_join
AS
SELECT pr1_0.pool_no                                                            as pool_number,
       pr1_0.return_date,
       pr1_0.owner,
       pr1_0.loc_code,
       pr1_0.pool_type,
       pr1_0.no_requested,
       count(*) FILTER (WHERE jp.owner = '400')                                 AS total_summoned,
       count(*) FILTER (WHERE jp.owner <> '400' and jp.is_active)               AS court_supply,
       count(*) FILTER (WHERE jp.owner = '400' and jp.status = 2)               AS available,
       count(*) FILTER (WHERE jp.owner = '400' and jp.status not in (1, 2, 11)) AS unavailable,
       count(*) FILTER (WHERE jp.owner = '400' and jp.status in (1, 11))        AS unresolved
FROM juror_mod.juror_pool jp
         JOIN juror_mod.juror j ON jp.juror_number::text = j.juror_number::text
         JOIN juror_mod.pool pr1_0 ON pr1_0.pool_no = jp.pool_number
WHERE j.summons_file IS NULL
   OR j.summons_file::text <> 'Disq. on selection'::text
GROUP BY pr1_0.pool_no,
         pr1_0.return_date,
         pr1_0.owner,
         pr1_0.loc_code,
         pr1_0.pool_type,
         pr1_0.no_requested;
