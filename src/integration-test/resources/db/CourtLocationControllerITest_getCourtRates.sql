delete
from juror_mod.court_location
where loc_code in ('001', '002', '003');

INSERT into juror_mod.court_location
(loc_code,
 rates_effective_from,
 rate_per_mile_car_0_passengers,
 rate_per_mile_car_1_passengers,
 rate_per_mile_car_2_or_more_passengers,
 rate_per_mile_motorcycle_0_passengers,
 rate_per_mile_motorcycle_1_or_more_passengers,
 rate_per_mile_bike,
 limit_financial_loss_half_day,
 limit_financial_loss_full_day,
 limit_financial_loss_half_day_long_trial,
 limit_financial_loss_full_day_long_trial,
 rate_substance_standard,
 rate_substance_long_day)
values ('001', '2023-01-05', 1.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012),
       ('002', '2023-05-06', 3.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012),
       ('003', '2023-09-06', 3.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012)
;


INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp)
VALUES (1, EXTRACT(EPOCH FROM current_date)),
       (2, EXTRACT(EPOCH FROM current_date)),
       (3, EXTRACT(EPOCH FROM current_date)),
       (4, EXTRACT(EPOCH FROM current_date)),
       (5, EXTRACT(EPOCH FROM current_date)),
       (6, EXTRACT(EPOCH FROM current_date)),
       (7, EXTRACT(EPOCH FROM current_date)),
       (8, EXTRACT(EPOCH FROM current_date))
;

INSERT into juror_mod.court_location_audit
(revision,
 rev_type,
 loc_code,
 rates_effective_from,
 rate_per_mile_car_0_passengers,
 rate_per_mile_car_1_passengers,
 rate_per_mile_car_2_or_more_passengers,
 rate_per_mile_motorcycle_0_passengers,
 rate_per_mile_motorcycle_1_or_more_passengers,
 rate_per_mile_bike,
 limit_financial_loss_half_day,
 limit_financial_loss_full_day,
 limit_financial_loss_half_day_long_trial,
 limit_financial_loss_full_day_long_trial,
 rate_substance_standard,
 rate_substance_long_day,
 public_transport_soft_limit)
values (1, 1, '001', '2023-01-05', 1.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012,
        13.013),
       (2, 1, '002', '2023-01-05', 2.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012,
        13.013),
       (3, 1, '002', '2023-05-06', 3.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012,
        13.013),
       (4, 1, '003', '2022-01-06', 1.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012,
        13.013),
       (5, 1, '003', '2023-08-06', 2.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012,
        13.013),
       (6, 1, '003', '2023-06-04', 5.01, 5.02, 5.03, 5.04, 5.05, 5.06, 5.07, 5.08, 5.09, 5.010, 5.011, 5.012, 5.013),
       (7, 1, '003', '2023-08-06', 4.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012,
        13.013),
       (8, 1, '003', '2023-09-06', 5.01, 2.02, 3.03, 4.04, 5.05, 6.06, 7.07, 8.08, 9.09, 10.010, 11.011, 12.012, 13.013)
;

