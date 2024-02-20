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

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, location, is_active)
values ('415', '641500020', '415230101', 2, '415', true),
       ('415', '641500021', '415230101', 2, '415', true),
       ('415', '641500022', '415230101', 2, '415', true),
       ('415', '641500023', '415230101', 2, '415', true),
       ('415', '641500024', '415230101', 2, '415', true);

--Approved edited
INSERT INTO juror_mod.appearance
(attendance_date, juror_number, loc_code, time_in, time_out, non_attendance,
 mileage_due, mileage_paid, travel_time, expense_submitted_date, sat_on_jury, pool_number, pay_cash,
 public_transport_total_due,
 public_transport_total_paid, hired_vehicle_total_due, hired_vehicle_total_paid,
 motorcycle_total_due, motorcycle_total_paid,
 car_total_due, car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid,
 parking_total_due, parking_total_paid, childcare_total_due, childcare_total_paid,
 misc_total_due, misc_total_paid, misc_description, loss_of_earnings_due, loss_of_earnings_paid,
 subsistence_due, subsistence_paid, smart_card_due, smart_card_paid, attendance_type, pay_attendance_type,
 is_draft_expense, f_audit,
 appearance_stage,
 travel_by_car, travel_jurors_taken_by_car,
 travel_by_motorcycle, travel_jurors_taken_by_motorcycle,
 travel_by_bicycle, miles_traveled, food_and_drink_claim_type)
values
    -- expenses for 641500020
    --3 Draft
    ('2023-01-05', '641500020', '415', '09:30', '16:00', false, 10, 0, '00:40', '2023-01-19', true, '415230101',
     true, 10, 0, 20, 0, 30, 0, 40, 0, 50, 0, 60, 0, 70, 0, 80, 0, 'Desc 1', 90, 0, 100, 0, 25, 0, 'FULL_DAY',
     'FULL_DAY', true, null, 'EXPENSE_ENTERED', true, 1, true, 2, true, 4, 'LESS_THAN_1O_HOURS'),
    ('2023-01-06', '641500020', '415', '09:30', '16:00', false, 10, 0, '00:40', '2023-01-19', true, '415230101',
     true, 11, 0, 21, 0, 31, 0, 41, 0, 51, 0, 61, 0, 71, 0, 81, 0, 'Desc 2', 91, 0, 101, 0, 26, 0, 'FULL_DAY',
     'FULL_DAY', true, null, 'EXPENSE_ENTERED', null, null, null, null, null, null, null),
    -- For approval
    ('2023-01-08', '641500020', '415', '09:00', '13:00', false, 10, 0, '00:40', '2023-01-19', true, '415230101',
     false, 13.97, 0, 23, 0, 33, 0, 43, 0, 53, 0, 63, 0, 73, 0, 83, 0, 'Desc 3', 93, 0, 103, 0, 28, 0, 'HALF_DAY',
     'HALF_DAY', false, '123', 'EXPENSE_ENTERED', null, null, false, null, true, 6, 'NONE'),
    -- For Approved
    ('2023-01-11', '641500020', '415', '09:35', '16:00', false, 0, 10, '01:43', '2023-01-19', true, '415230101',
     false, 103, 93, 93, 93, 83, 83, 73, 73, 63, 63, 53, 53, 43, 43, 33, 33, 'Desc 4', 23, 23, 13, 13, 28.52, 28.52,
     'FULL_DAY',
     'FULL_DAY', false, '321', 'EXPENSE_AUTHORISED', true, 3, null, null, null, 9, 'MORE_THAN_10_HOURS')
;