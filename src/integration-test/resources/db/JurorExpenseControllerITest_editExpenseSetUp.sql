INSERT INTO juror_mod.rev_info
    (revision_number, revision_timestamp)
VALUES (0, EXTRACT(EPOCH FROM current_date)),
       (1, EXTRACT(EPOCH FROM current_date)),
       (2, EXTRACT(EPOCH FROM current_date)),
       (3, EXTRACT(EPOCH FROM current_date)),
       (4, EXTRACT(EPOCH FROM current_date)),
       (5, EXTRACT(EPOCH FROM current_date));
SELECT setval('juror_mod.rev_info_seq', 5, true);

SELECT setval('juror_mod.appearance_f_audit_seq', 1, true);

INSERT INTO juror_mod.users (owner, username, name, level, active, team_id, version, password)
VALUES ('415', 'COURT_USER', 'Court User', 0, true, 1, 1, '5baa61e4c9b93f3f'),
       ('400', 'BUREAU_USER', 'Bureau User', 1, true, 1, 1, '5baa61e4c9b93f3f');
INSERT INTO juror_mod.court_location_audit (revision, rev_type, loc_code, rate_per_mile_car_0_passengers,
                                            rate_per_mile_car_1_passengers, rate_per_mile_car_2_or_more_passengers,
                                            rate_per_mile_motorcycle_0_passengers,
                                            rate_per_mile_motorcycle_1_or_more_passengers, rate_per_mile_bike,
                                            limit_financial_loss_half_day, limit_financial_loss_full_day,
                                            limit_financial_loss_half_day_long_trial,
                                            limit_financial_loss_full_day_long_trial, public_transport_soft_limit,
                                            rate_subsistence_standard, rate_subsistence_long_day, rates_effective_from)
VALUES (0, 2, '415', 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.6, 0.7, 0.8, 0.9, 0.11, 0.12, 0.13, '2023-01-01');

-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode,
                             responded)
values ('641500020', 'Lnametwozero', 'Fnametwozero', current_date - interval '20 years', '520 Street Name', 'Any town',
        'CH1 2AN', 'Y'),
       ('641500021', 'Lnametwoone', 'Fnametwoone', current_date - interval '21 years', '521 Street Name', 'Any town',
        'CH1 2AN', 'Y'),
       ('641500022', 'Lnametwotwo', 'Fnametwotwo', current_date - interval '22 years', '522 Street Name', 'Any town',
        'CH1 2AN', 'Y'),
       ('641500023', 'Lnametwothree', 'Fnametwothree', current_date - interval '23 years', '523 Street Name',
        'Any town', 'CH1 2AN', 'Y'),
       ('641500024', 'Lnametwofour', 'Fnametwofour', current_date - interval '24 years', '524 Street Name', 'Any town',
        'CH1 2AN', 'Y');
insert into juror_mod.juror_audit (revision, rev_type, juror_number, last_name, first_name, dob, address_line_1,
                                   address_line_4, postcode)
values (1, 2, '641500021', 'Lnametwoone', 'Fnametwoone', current_date - interval '21 years', '521 Street Name',
        'Any town', 'CH1 2AN'),
       (2, 2, '641500020', 'Lnametwozero', 'Fnametwozero', current_date - interval '20 years', '520 Street Name',
        'Any town', 'CH1 2AN');


update juror_mod.court_location
SET rate_per_mile_car_0_passengers                = 0.314,
    rate_per_mile_car_1_passengers                = 0.356,
    rate_per_mile_car_2_or_more_passengers        = 0.398,
    rate_per_mile_motorcycle_0_passengers         = 0.314,
    rate_per_mile_motorcycle_1_or_more_passengers = 0.324,
    rate_per_mile_bike                            = 0.096,
    limit_financial_loss_half_day                 = 32.47,
    limit_financial_loss_full_day                 = 64.95,
    limit_financial_loss_half_day_long_trial      = 64.95,
    limit_financial_loss_full_day_long_trial      = 129.91,
    rate_subsistence_standard                       = 5.71,
    rate_subsistence_long_day                       = 12.17
WHERE loc_code = '415';


-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, location, is_active)
values ('415', '641500020', '415230101', 2, '415', true),
       ('415', '641500021', '415230101', 2, '415', true),
       ('415', '641500022', '415230101', 2, '415', true),
       ('415', '641500023', '415230101', 2, '415', true),
       ('415', '641500024', '415230101', 2, '415', true);



INSERT INTO juror_mod.users (owner, username, name, level, active, team_id, version, password)
VALUES ('415', 'smcintyre', 'Stephanie Mcintyre', 0, true, 1, 1, '5baa61e4c9b93f3f'),
       ('415', 'sbell', 'Sandra Bell', 0, true, 2, 1, '5baa61e4c9b93f3f'),
       ('415', 'alineweaver', 'Albert Lineweaver', 0, true, 3, 1, '5baa61e4c9b93f3f'),
       ('416', 'jbrown1', 'Jared Brown', 1, true, 1, 1, '5baa61e4c9b93f3f'),
       ('417', 'jwilliams', 'Jeremy Williams', 1, true, 2, 1, '5baa61e4c9b93f3f'),
       ('418', 'jbrown', 'John Brown', 1, true, 3, 1, '5baa61e4c9b93f3f');

--Approved edited

INSERT INTO juror_mod.appearance
(attendance_date, juror_number, loc_code, time_in, time_out, non_attendance,
 travel_time, sat_on_jury, pool_number, pay_cash,
 public_transport_total_due,
 public_transport_total_paid, hired_vehicle_total_due, hired_vehicle_total_paid,
 motorcycle_total_due, motorcycle_total_paid,
 car_total_due, car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid,
 parking_total_due, parking_total_paid, childcare_total_due, childcare_total_paid,
 misc_total_due, misc_total_paid, loss_of_earnings_due, loss_of_earnings_paid,
 subsistence_due, subsistence_paid, smart_card_due, smart_card_paid, attendance_type, pay_attendance_type,
 is_draft_expense, f_audit,
 appearance_stage)
values

    --3 For Approval
    ('2023-01-08', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 13.97, 0, 23, 0, 33, 0, 43, 0, 53, 0, 63, 0, 73, 0, 83, 0, 93, 0, 103, 0, 28, 0, 'FULL_DAY', 'FULL_DAY',
     false, '123', 'EXPENSE_ENTERED'),
    ('2023-01-09', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 14.01, 0, 24, 0, 34, 0, 44, 0, 54, 0, 64, 0, 74, 0, 84, 0, 94, 0, 104, 0, 29, 0, 'FULL_DAY', 'FULL_DAY',
     false, '123', 'EXPENSE_ENTERED'),
    ('2023-01-10', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 15, 0, 25, 0, 35, 0, 45, 0, 55, 0, 65, 0, 75, 0, 85, 0, 95, 0, 105, 0, 30, 0, 'FULL_DAY', 'FULL_DAY', false,
     '123', 'EXPENSE_ENTERED'),
    -- 3 Approved
    ('2023-01-11', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 1, 1, 5, 5, 0, 0, 1, 1, 0, 0, 12, 12, 4, 4, 5, 5, 6, 6, 3, 3, 29, 29, 'FULL_DAY',
     'FULL_DAY', false, '321', 'EXPENSE_AUTHORISED'),
    ('2023-01-12', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 104, 104, 5, 5, 84, 84, 1, 1, 64, 64, 12, 12, 4, 4, 5, 5, 6, 6, 3, 3, 29, 29, 'FULL_DAY',
     'FULL_DAY', false, '321', 'EXPENSE_AUTHORISED'),
    ('2023-01-13', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 105, 105, 95, 95, 85, 85, 75, 75, 65, 65, 55, 55, 45, 45, 35, 35, 25, 25, 15, 15, 30, 30, 'FULL_DAY',
     'FULL_DAY', false, '321', 'EXPENSE_AUTHORISED'),
    -- 2 Approved - 1 edited
    ('2023-01-14', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 103, 103, 93, 93, 83, 83, 73, 73, 63, 63, 53, 53, 43, 43, 33, 33, 23, 23, 13, 13, 28, 28, 'FULL_DAY',
     'FULL_DAY', false, '12345', 'EXPENSE_AUTHORISED'),
    ('2023-01-15', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 1, 1, 5, 5, 0, 0, 1, 1, 0, 0, 12, 12, 4, 4, 5, 5, 6, 6, 3, 3, 29, 29, 'FULL_DAY',
     'FULL_DAY', false, '12345', 'EXPENSE_EDITED'),
    ('2023-01-16', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 105, 105, 95, 95, 85, 85, 75, 75, 65, 65, 55, 55, 45, 45, 35, 35, 25, 25, 15, 15, 30, 30, 'FULL_DAY',
     'FULL_DAY', false, '12345', 'EXPENSE_AUTHORISED')
;