-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
new_request, attend_time)
values ('415', '415230101', '2024-01-02', 5, 5, 'CRO', '415', 'N', '2024-01-02 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode,
                             responded)
values ('641500020', 'Lnametwozero', 'Fnametwozero', current_date - interval '20 years', '520 Street Name', 'Any town',
'CH1 2AN', 'Y');

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active)
values ('415', '641500020', '415230101', 2, true);


INSERT INTO juror_mod.users (owner, username, name, level, active,team_id,version, password)
VALUES
    ('415','COURT_USER', 'Court User', 0, true, 1, 1,'5baa61e4c9b93f3f'),
    ('400','BUREAU_USER', 'Bureau User', 1, true, 1, 1,'5baa61e4c9b93f3f');

INSERT INTO juror_mod.appearance
(attendance_date, juror_number, loc_code, time_in, time_out, non_attendance, mileage_due, mileage_paid, travel_time,
expense_submitted_date, sat_on_jury, pool_number, pay_cash, public_transport_total_due,
public_transport_total_paid, hired_vehicle_total_due, hired_vehicle_total_paid, motorcycle_total_due,
motorcycle_total_paid, car_total_due, car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid,
parking_total_due, parking_total_paid, childcare_total_due, childcare_total_paid, misc_total_due, misc_total_paid,
loss_of_earnings_due, loss_of_earnings_paid, subsistence_due, subsistence_paid, smart_card_due, smart_card_paid,
attendance_type, is_draft_expense, f_audit, appearance_stage)
values
-- expenses for 641500020
-- 3 Draft
('2024-01-02', '641500020', '415', '09:30', '16:00', false, 10, 0, 0.66, '2024-01-19', true, '415230101',
false, 10, 0, 20, 0, 30, 0, 40, 0, 50, 0, 60, 0, 70, 0, 80, 0, 90, 0, 100, 0, 25, 0, 'FULL_DAY', true,
null, 'EXPENSE_ENTERED'),
('2024-01-03', '641500020', '415', '09:30', '16:00', false, 10, 0, 0.66, '2024-01-19', true, '415230101',
true, 11, 0, 21, 0, 31, 0, 41, 0, 51, 0, 61, 0, 71, 0, 81, 0, 91, 0, 101, 0, 26, 0, 'FULL_DAY', true,
null, 'EXPENSE_ENTERED'),
('2024-01-04', '641500020', '415', '09:30', '16:00', false, 10, 0, 0.66, '2024-01-19', true, '415230101',
false, 12, 0, 22, 0, 32, 0, 42, 0, 52, 0, 62, 0, 72, 0, 82, 0, 92, 0, 102, 0, 27, 0, 'HALF_DAY', true,
null, 'EXPENSE_ENTERED');