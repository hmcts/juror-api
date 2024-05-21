INSERT INTO juror_mod.financial_audit_details(id, juror_revision,
                                              court_location_revision, type, created_by, created_on, juror_number,
                                              loc_code)
VALUES (12345, 1, 1, 'FOR_APPROVAL_EDIT', 'COURT_USER2', '2023-01-11 09:31:01.000',
        '641500020', '415'),
       (12344, 1, 1, 'APPROVED_BACS', 'COURT_USER3', '2023-01-10 09:31:01.000',
        '641500020', '415');
INSERT INTO juror_mod.financial_audit_details_appearances (financial_audit_id, loc_code,
                                                           attendance_date, appearance_version)
VALUES (12344, '415', '2023-01-14', 1),
       (12344, '415', '2023-01-15', 1),
       (12344, '415', '2023-01-16', 1),
       (12345, '415', '2023-01-14', 2),
       (12345, '415', '2023-01-15', 2),
       (12345, '415', '2023-01-16', 2);
insert into juror_mod.appearance_audit
(revision, rev_type, version, attendance_date, juror_number, loc_code, time_in, time_out, non_attendance, travel_time,
 sat_on_jury, pool_number, pay_cash, public_transport_total_due, public_transport_total_paid, hired_vehicle_total_due,
 hired_vehicle_total_paid, motorcycle_total_due, motorcycle_total_paid, car_total_due, car_total_paid,
 pedal_cycle_total_due, pedal_cycle_total_paid, parking_total_due, parking_total_paid, childcare_total_due,
 childcare_total_paid, misc_total_due, misc_total_paid, loss_of_earnings_due, loss_of_earnings_paid, subsistence_due,
 subsistence_paid, smart_card_due, smart_card_paid, attendance_type, is_draft_expense, f_audit, appearance_stage)
VALUES (0, 1, 1, '2023-01-14', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101', false, 0, 103,
        0, 93, 0, 83, 0, 73, 0, 63, 0, 53, 0, 43, 0, 33, 0, 23, 0, 13, 0, 28, 'FULL_DAY', false, '12344',
        'EXPENSE_AUTHORISED'),
       (0, 1, 1, '2023-01-15', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101', false, 0, 104,
        0, 94, 0, 84, 0, 74, 0, 64, 0, 54, 0, 44, 0, 34, 0, 24, 0, 14, 0, 29, 'FULL_DAY', false, '12344',
        'EXPENSE_AUTHORISED'),
       (0, 1, 1, '2023-01-16', '641500020', '415', '09:30', '16:00', false, '00:40', true, '415230101', false, 0, 105,
        0, 95, 0, 85, 0, 75, 0, 65, 0, 55, 0, 45, 0, 35, 0, 25, 0, 15, 0, 30, 'FULL_DAY', false, '12344',
        'EXPENSE_AUTHORISED');