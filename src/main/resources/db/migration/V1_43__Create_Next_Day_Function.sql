CREATE OR REPLACE FUNCTION juror_mod.weekdayname_to_dow(weekdayname VARCHAR) RETURNS INT AS
$$
SELECT
  CASE UPPER(weekdayname)
    WHEN 'SUNDAY'
      THEN 0
    WHEN 'MONDAY'
      THEN 1
    WHEN 'TUESDAY'
      THEN 2
    WHEN 'WEDNESDAY'
      THEN 3
    WHEN 'THURSDAY'
      THEN 4
    WHEN 'FRIDAY'
      THEN 5
    WHEN 'SATURDAY'
      THEN 6
  END AS ret_val
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION juror_mod.next_day(date_input DATE, weekdayname VARCHAR) RETURNS DATE AS
$$
SELECT $1::DATE + COALESCE(NULLIF((7 + juror_mod.weekdayname_to_dow($2) - EXTRACT(DOW FROM $1::DATE))::INT % 7, 0), 7) AS result
$$ LANGUAGE sql;
