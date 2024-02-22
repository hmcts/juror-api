-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror record
insert into juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode,
                             responded, smart_card_number, mileage, travel_time, financial_loss,claiming_subsistence_allowance)
values ('641500020', 'Lnametwozero', 'Fnametwozero', current_date - interval '20 years', '520 Street Name', 'Any town',
        'CH1 2AN',
        'Y', '12345678', '6', '04:30', '39.12',true);

-- create juror_pool associative record
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active)
values ('415', '641500020', '415230101', 2, true);

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
    -- expenses for 641500020
    --3 Draft
    ('2023-01-05', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 10, 0, 20, 0, 30, 0, 40, 0, 50, 0, 60, 0, 70, 0, 80, 0, 90, 0, 100, 0, 25, 0, 'FULL_DAY', 'FULL_DAY', true,
     null, 'EXPENSE_ENTERED');