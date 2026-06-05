DELETE FROM juror_mod.stats_sitting_days;

INSERT INTO juror_mod.stats_sitting_days
(service_year, service_month, court_code, sitting_days_category, court_name, number_of_sitting_days, number_of_jurors)
VALUES
    ('2024', '2024-05', '415', '0', 'CHESTER', 0, 10),
    ('2024', '2024-05', '415', '1', 'CHESTER', 2, 5),
    ('2024', '2024-05', '415', '2', 'CHESTER', 3, 4),
    ('2024', '2024-05', '415', '3', 'CHESTER', 50, 45),
    ('2024', '2024-05', '415', '4', 'CHESTER', 100, 90),
    ('2024', '2024-05', '415', '5', 'CHESTER', 5, 5),
    ('2024', '2024-05', '415', '6', 'CHESTER', 5, 3),
    ('2024', '2024-05', '415', '7', 'CHESTER', 5, 2),
    ('2024', '2024-05', '415', '8', 'CHESTER', 3, 2),
    ('2024', '2024-05', '415', '9', 'CHESTER', 2, 2),
    ('2024', '2024-05', '415', '10', 'CHESTER', 6, 5),
    ('2024', '2024-05', '415', '11 or more', 'CHESTER', 11, 10);
