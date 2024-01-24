-- juror_mod.pool_stats source

CREATE OR REPLACE VIEW juror_mod.pool_stats
AS SELECT jp.pool_number,
    sum(
        CASE
            WHEN jp.owner = '400' THEN 1
            ELSE 0
        END) AS total_summoned,
    sum(
        CASE
            WHEN jp.owner <> '400' AND jp.is_active = 'Y' THEN 1
            ELSE 0
        END) AS court_supply,
    sum(
        CASE
            WHEN jp.owner = '400' AND jp.status = 2 THEN 1
            ELSE 0
        END) AS available,
    sum(
        CASE
            WHEN jp.owner = '400' AND (jp.status <> ALL (ARRAY[1, 2, 11])) THEN 1
            ELSE 0
        END) AS unavailable,
    sum(
        CASE
            WHEN jp.owner = '400' AND (jp.status = ANY (ARRAY[1, 11])) THEN 1
            ELSE 0
        END) AS unresolved
   FROM juror_mod.juror_pool jp
     INNER JOIN juror_mod.juror j ON jp.juror_number = j.juror_number
  WHERE j.summons_file IS NULL OR j.summons_file <> 'Disq. on selection'
  GROUP BY jp.pool_number;