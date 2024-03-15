-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode,
                             responded, mileage)
values ('641500020', 'Lnametwozero', 'Fnametwozero', current_date - interval '20 years', '520 Street Name', 'Any town',
        'CH1 2AN', 'Y', 2),
       ('641500021', 'Lnametwoone', 'Fnametwoone', current_date - interval '21 years', '521 Street Name', 'Any town',
        'CH1 2AN', 'Y', 3),
       ('641500022', 'Lnametwotwo', 'Fnametwotwo', current_date - interval '22 years', '522 Street Name', 'Any town',
        'CH1 2AN', 'Y', 5),
       ('641500023', 'Lnametwothree', 'Fnametwothree', current_date - interval '23 years', '523 Street Name',
        'Any town', 'CH1 2AN', 'Y', 6),
       ('641500024', 'Lnametwofour', 'Fnametwofour', current_date - interval '24 years', '524 Street Name', 'Any town',
        'CH1 2AN', 'Y', 7);

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active)
values ('415', '641500020', '415230101', 2, true),
       ('415', '641500021', '415230101', 2, true),
       ('415', '641500022', '415230101', 2, true),
       ('415', '641500023', '415230101', 2, true),
       ('415', '641500024', '415230101', 2, true);

INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp)
VALUES (1, EXTRACT(EPOCH FROM current_date)),--Appearance
       (2, EXTRACT(EPOCH FROM current_date)),--Appearance
       (3, EXTRACT(EPOCH FROM current_date)),--juror
       (4, EXTRACT(EPOCH FROM current_date));--juror;


INSERT INTO juror_mod.users (owner, username,email, name, active, team_id, version)
VALUES ('415', 'smcintyre','smcintyre@email.gov.uk', 'Stephanie Mcintyre', true, 1, 1),
       ('415', 'sbell','sbell@email.gov.uk', 'Sandra Bell', true, 2, 1),
       ('415', 'alineweaver','alineweaver@email.gov.uk', 'Albert Lineweaver', true, 3, 1),
       ('416', 'jbrown1','jbrown1@email.gov.uk', 'Jared Brown', true, 1, 1),
       ('417', 'jwilliams','jwilliams@email.gov.uk', 'Jeremy Williams', true, 2, 1),
       ('418', 'jbrown','jbrown@email.gov.uk', 'John Brown', true, 3, 1);

INSERT INTO juror_mod.financial_audit_details(id, submitted_on, submitted_by, approved_on, approved_by,
                                              juror_revision_when_approved)
VALUES (123, TIMESTAMP '2024-01-09 10:00:00', 'smcintyre', null, null, null),--For approval
       (321, TIMESTAMP '2024-01-09 11:11:11', 'sbell', TIMESTAMP '2024-01-10 12:00:00', 'alineweaver', 3), --Approved
       (12345, TIMESTAMP '2024-01-09 12:12:12', 'sbell', TIMESTAMP '2024-01-10 12:00:01', 'alineweaver', null);
--Approved edited

INSERT INTO juror_mod.appearance
(attendance_date, juror_number, loc_code, time_in, time_out, non_attendance,
  travel_time, expense_submitted_date, sat_on_jury, pool_number, pay_cash,
 public_transport_total_due,
 public_transport_total_paid, hired_vehicle_total_due, hired_vehicle_total_paid,
 motorcycle_total_due, motorcycle_total_paid,
 car_total_due, car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid,
 parking_total_due, parking_total_paid, childcare_total_due, childcare_total_paid,
 misc_total_due, misc_total_paid, loss_of_earnings_due, loss_of_earnings_paid,
 subsistence_due, subsistence_paid, smart_card_due, smart_card_paid, attendance_type, is_draft_expense, f_audit,
 appearance_stage)
values
    -- expenses for 641500020
    --3 Draft
    ('2023-01-05', '641500020', '415', '09:30', '16:00', false,  '00:40', true, '415230101',
     false, 10, 0, 20, 0, 30, 0, 40, 0, 50, 0, 60, 0, 70, 0, 80, 0, 90, 0, 100, 0, 25, 0, 'FULL_DAY', true,
     null, 'EXPENSE_ENTERED'),
    ('2023-01-06', '641500020', '415', '09:30', '16:00', false,  '00:40', true, '415230101',
     true, 11, 0, 21, 0, 31, 0, 41, 0, 51, 0, 61, 0, 71, 0, 81, 0, 91, 0, 101, 0, 26, 0, 'FULL_DAY', true,
     null, 'EXPENSE_ENTERED'),
    ('2023-01-07', '641500020', '415', '09:30', '16:00', false,  '00:40', true, '415230101',
     false, 12, 0, 22, 0, 32, 0, 42, 0, 52, 0, 62, 0, 72, 0, 82, 0, 92, 0, 102, 0, 27, 0, 'HALF_DAY', true,
     null, 'EXPENSE_ENTERED'),
    --3 For Approval
    ('2023-01-08', '641500020', '415', '09:30', '16:00', false,  '00:40', true, '415230101',
     false, 13.97, 0, 23, 0, 33, 0, 43, 0, 53, 0, 63, 0, 73, 0, 83, 0, 93, 0, 103, 0, 28, 0, 'FULL_DAY', false,
     '123', 'EXPENSE_ENTERED'),
    ('2023-01-09', '641500020', '415', '09:30', '16:00', false,  '00:40', true, '415230101',
     false, 14.01, 0, 24, 0, 34, 0, 44, 0, 54, 0, 64, 0, 74, 0, 84, 0, 94, 0, 104, 0, 29, 0, 'FULL_DAY', false,
     '123', 'EXPENSE_ENTERED'),
    ('2023-01-10', '641500020', '415', '09:30', '16:00', false,  '00:40', true, '415230101',
     false, 15, 0, 25, 0, 35, 0, 45, 0, 55, 0, 65, 0, 75, 0, 85, 0, 95, 0, 105, 0, 30, 0, 'FULL_DAY', false,
     '123', 'EXPENSE_ENTERED'),
    -- 3 Approved
    ('2023-01-11', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 0, 103, 0, 93, 0, 83, 0, 73, 0, 63, 0, 53, 0, 43, 0, 33, 0, 23, 0, 13, 0, 28, 'FULL_DAY', false,
     '321', 'EXPENSE_AUTHORISED'),
    ('2023-01-12', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 0, 104, 0, 94, 0, 84, 0, 74, 0, 64, 0, 54, 0, 44, 0, 34, 0, 24, 0, 14, 0, 29, 'FULL_DAY', false,
     '321', 'EXPENSE_AUTHORISED'),
    ('2023-01-13', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 0, 105, 0, 95, 0, 85, 0, 75, 0, 65, 0, 55, 0, 45, 0, 35, 0, 25, 0, 15, 0, 30, 'FULL_DAY', false,
     '321', 'EXPENSE_AUTHORISED'),
    -- 2 Approved - 1 edited
    ('2023-01-14', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 0, 103, 0, 93, 0, 83, 0, 73, 0, 63, 0, 53, 0, 43, 0, 33, 0, 23, 0, 13, 0, 28, 'FULL_DAY', false,
     '12345', 'EXPENSE_AUTHORISED'),
    ('2023-01-15', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 30, 104, 4, 94, 11, 84, 0, 74, 19.45, 64, 13, 54, 0.44, 44, 2.03, 34, 0.01, 24, 3.93, 14, 0, 29,
     'FULL_DAY', false,
     '12345', 'EXPENSE_EDITED'),
    ('2023-01-16', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 0, 105, 0, 95, 0, 85, 0, 75, 0, 65, 0, 55, 0, 45, 0, 35, 0, 25, 0, 15, 0, 30, 'FULL_DAY', false,
     '12345', 'EXPENSE_AUTHORISED'),

-- negative
    ('2023-01-16', '641500022', '415', '09:30', '16:00', false, '00:40', true, '415230101',
     false, 0, 105, 0, 95, 0, 85, 0, 75, 0, 65, 0, 55, 0, 45, 0, 35, 0, 25, 0, 15, 0, 30, 'FULL_DAY', true,
     '12345', 'EXPENSE_ENTERED')
;



INSERT INTO juror_mod.appearance_audit
(revision, rev_type,
 attendance_date, juror_number, loc_code, time_in, time_out, non_attendance,
 travel_time, sat_on_jury, pool_number, pay_cash,
 public_transport_total_due,
 public_transport_total_paid, hired_vehicle_total_due, hired_vehicle_total_paid,
 motorcycle_total_due, motorcycle_total_paid,
 car_total_due, car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid,
 parking_total_due, parking_total_paid, childcare_total_due, childcare_total_paid,
 misc_total_due, misc_total_paid, loss_of_earnings_due, loss_of_earnings_paid,
 subsistence_due, subsistence_paid, smart_card_due, smart_card_paid, attendance_type, is_draft_expense, f_audit,
 appearance_stage)
values (1, 1, '2023-01-15', '641500020', '415', '09:30', '16:00', false, '00:40',  true,
        '415230101', false, 0, 104, 0, 94, 0, 84, 0, 74, 0, 64, 0, 54, 0, 44, 0, 34, 0, 24, 0, 14, 0, 29, 'FULL_DAY',
        false, '321', 'EXPENSE_AUTHORISED')