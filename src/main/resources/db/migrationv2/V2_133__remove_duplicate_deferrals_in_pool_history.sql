DELETE FROM juror_mod.pool_history AS earlier
WHERE earlier.history_code = 'PHDI'
  AND earlier.history_date >= TIMESTAMP '2024-07-01 00:00:00'
  AND earlier.other_information ~ '^[0-9]+ \(New Pool Request\)$'
  AND EXISTS (
      SELECT 1
      FROM juror_mod.pool_history AS later
      WHERE later.id > earlier.id
        AND later.pool_no = earlier.pool_no
        AND later.user_id = earlier.user_id
        AND later.history_code = 'PHDI'
        AND later.history_date > earlier.history_date
        AND later.history_date <= earlier.history_date + INTERVAL '1 second'
        AND later.other_information IN (
            earlier.other_information,
            REPLACE(earlier.other_information, ' (New Pool Request)', ' (Add Pool Request)')
        )
  );
