INSERT INTO juror_mod.holiday(id, owner, holiday, description, public)
VALUES (10000, null, to_date(extract(year from current_date) - 1 || '-01-01', 'YYYY-MM-DD'), 'Public holiday 1 -1',
        true),
       (10001, null, to_date(extract(year from current_date) || '-01-01', 'YYYY-MM-DD'), 'Public holiday 1', true),
       (10002, null, to_date(extract(year from current_date) + 1 || '-01-01', 'YYYY-MM-DD'), 'Public holiday 1 + 1',
        true),
       (10003, null, to_date(extract(year from current_date) + 2 || '-01-01', 'YYYY-MM-DD'), 'Public holiday 1 + 2',
        true),


       (10004, null, to_date(extract(year from current_date) - 1 || '-02-01', 'YYYY-MM-DD'), 'Public holiday 2 -1',
        true),
       (10005, null, to_date(extract(year from current_date) || '-02-01', 'YYYY-MM-DD'), 'Public holiday 2', true),
       (10006, null, to_date(extract(year from current_date) + 1 || '-02-01', 'YYYY-MM-DD'), 'Public holiday 2 + 1',
        true),
       (10007, null, to_date(extract(year from current_date) + 2 || '-02-01', 'YYYY-MM-DD'), 'Public holiday 2 + 2',
        true),

       (10008, '415', to_date(extract(year from current_date) - 1 || '-01-01', 'YYYY-MM-DD'), 'Court holiday 1 -1',
        false),
       (10009, '415', to_date(extract(year from current_date) || '-01-01', 'YYYY-MM-DD'), 'Court holiday 1', false),
       (10010, '416', to_date(extract(year from current_date) + 1 || '-01-01', 'YYYY-MM-DD'), 'Court holiday 1 + 1',
        false),
       (10011, '415', to_date(extract(year from current_date) + 2 || '-01-01', 'YYYY-MM-DD'), 'Court holiday 1 + 2',
        false),


       (10012, '415', to_date(extract(year from current_date) - 1 || '-02-01', 'YYYY-MM-DD'), 'Court holiday 2 -1',
        false),
       (10013, '416', to_date(extract(year from current_date) || '-02-01', 'YYYY-MM-DD'), 'Court holiday 2', false),
       (10014, '415', to_date(extract(year from current_date) + 1 || '-02-01', 'YYYY-MM-DD'), 'Court holiday 2 + 1',
        false),
       (10015, '415', to_date(extract(year from current_date) + 2 || '-02-01', 'YYYY-MM-DD'), 'Court holiday 2 + 2',
        false)
;
