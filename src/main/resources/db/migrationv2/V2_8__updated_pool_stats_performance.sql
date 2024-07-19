CREATE INDEX juror_summons_file_idx ON juror_mod.juror (summons_file);

CREATE OR REPLACE VIEW juror_mod.pool_stats
AS SELECT jp.pool_number,
          count(*) FILTER (WHERE owner = '400') AS total_summoned,
          count(*) FILTER (WHERE owner <> '400' and jp.is_active) AS court_supply,
          count(*) FILTER (WHERE owner = '400' and jp.status = 2) AS available,
          count(*) FILTER (WHERE owner = '400' and jp.status not in (1,2,11)) AS unavailable,
          count(*) FILTER (WHERE owner = '400' and jp.status in (1, 11)) AS unresolved
   FROM juror_mod.juror_pool jp
            JOIN juror_mod.juror j ON jp.juror_number::text = j.juror_number::text
   WHERE j.summons_file IS NULL OR j.summons_file::text <> 'Disq. on selection'::text
   GROUP BY jp.pool_number;
