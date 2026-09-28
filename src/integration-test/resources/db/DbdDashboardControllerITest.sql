DELETE FROM juror_dashboard.dbd_response_stats;

INSERT INTO juror_dashboard.dbd_response_stats (
    summons_date,
    response_date,
    response_period,
    loc_code,
    response_method,
    age_group,
    juror_count
) VALUES
    ('2026-01-05', '2026-01-08', 'Within 7 days', '415', 'Online', '18-24', 8),
    ('2026-01-05', '2026-01-15', 'Within 14 days', '415', 'Paper', '25-34', 6),
    ('2026-01-05', NULL, 'Over 21 days', '415', 'None', '35-44', 9),
    ('2026-02-05', '2026-02-08', 'Within 7 days', '415', 'Online', '18-24', 4),
    ('2026-02-05', '2026-02-20', 'Within 21 days', '415', 'Paper', '25-34', 2),
    ('2026-02-05', NULL, 'Over 21 days', '415', 'None', '35-44', 6);
