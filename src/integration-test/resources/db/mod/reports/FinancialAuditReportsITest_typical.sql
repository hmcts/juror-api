INSERT INTO juror_mod.rev_info
    (revision_number, revision_timestamp)
VALUES (1, 1712966318098),
       (42180, 1712966318098),
       (42257, 1712966318098),
       (105464, 1712966318098),
       (105462, 1712966318098),
       (105469, 1712966318098),
       (105466, 1712966318098),
       (105465, 1712966318098),
       (105463, 1712966318098),
       (105460, 1712966318098),
       (105468, 1712966318098),
       (105461, 1712966318098),
       (105467, 1712966318098)
;
INSERT INTO juror_mod.court_location_audit
(revision, rev_type, loc_code, public_transport_soft_limit, taxi_soft_limit)
VALUES (1, 0, '415', NULL, NULL);
INSERT INTO juror_mod.expense_rates
(id, rate_per_mile_car_0_passengers, rate_per_mile_car_1_passengers, rate_per_mile_car_2_or_more_passengers,
 rate_per_mile_motorcycle_0_passengers, rate_per_mile_motorcycle_1_or_more_passengers, rate_per_mile_bike,
 limit_financial_loss_half_day, limit_financial_loss_full_day, limit_financial_loss_half_day_long_trial,
 limit_financial_loss_full_day_long_trial, rate_subsistence_standard, rate_subsistence_long_day)
VALUES (2, 0.31400, 0.35600, 0.39800, 0.31400, 0.32400, 0.09600, 32.47000, 64.95000, 64.95000, 129.91000, 5.71000,
        12.17000);

INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update, additional_summons,
 attend_time, nil_pool, total_no_required, date_created)
VALUES ('415240303', '415', '2024-03-25', 60, 'CRO', '415', 'N', '2024-04-12 23:58:38.000', NULL,
        '2024-03-25 08:30:00.000', false, 60, '2024-04-12 23:58:18.702');
INSERT INTO juror_mod.juror
(juror_number, poll_number, title, last_name, first_name, dob, address_line_1, address_line_2, address_line_3,
 address_line_4, address_line_5, postcode, h_phone, w_phone, w_ph_local, responded, date_excused, excusal_code, acc_exc,
 date_disq, disq_code, user_edtq, notes, no_def_pos, perm_disqual, reasonable_adj_code, reasonable_adj_msg,
 smart_card_number, completion_date, sort_code, bank_acct_name, bank_acct_no, bldg_soc_roll_no, welsh, police_check,
 last_update, summons_file, m_phone, h_email, contact_preference, notifications, date_created, optic_reference,
 pending_title, pending_first_name, pending_last_name, mileage, financial_loss, travel_time, bureau_transfer_date,
 claiming_subsistence_allowance, service_comp_comms_status, login_attempts, is_locked)
VALUES ('200106974', NULL, 'Ms', 'Gallus', 'Josh', '1976-06-05 00:00:00.000', '68 EWP Junction', 'Box Number 0',
        'Canterbury', 'Shetland (Zetland)', NULL, 'BB2 2TG', NULL, NULL, NULL, true, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, 'N', 'Sample Details 1', NULL, '2024-04-08 00:00:00.000', '901234', 'Josh Gallus', '12345678',
        NULL, NULL, 'ELIGIBLE', '2024-04-16 18:18:03.000', NULL, NULL, NULL, NULL, 0, '2024-04-12 23:58:34.224', NULL,
        NULL, NULL, NULL, 9, NULL, NULL, NULL, false, NULL, 0, false);


INSERT INTO juror_mod.juror_audit
(revision, juror_number, rev_type, title, first_name, last_name, dob, address_line_1, address_line_2, address_line_3,
 address_line_4, address_line_5, address6, postcode, h_email, h_phone, m_phone, w_phone, w_ph_local, bank_acct_name,
 bank_acct_no, bldg_soc_roll_no, sort_code, pending_title, pending_first_name, pending_last_name,
 claiming_subsistence_allowance, smart_card_number, mileage)
VALUES (42180, '200106974', 0, 'Ms', 'Josh', 'Gallus', NULL, '68 EWP Junction', 'Box Number 0', 'Canterbury',
        'Shetland (Zetland)', NULL, NULL, 'BB2 2TG', NULL, NULL, NULL, NULL, NULL, 'Josh Gallus', '12345678', NULL,
        '901234', NULL, NULL, NULL, false, NULL, 10),
       (42257, '200106974', 1, 'Ms', 'Josh', 'Gallus', '1976-06-05', '68 EWP Junction', 'Box Number 0', 'Canterbury',
        'Shetland (Zetland)', NULL, NULL, 'BB2 2TG', NULL, NULL, NULL, NULL, NULL, 'Josh Gallus', '12345678', NULL,
        '901234', NULL, NULL, NULL, false, NULL, 11);

INSERT INTO juror_mod.juror_pool
(juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date, "location", no_attendances,
 no_attended, no_fta, no_awol, pool_seq, edit_tag, next_date, on_call, smart_card, was_deferred, deferral_code,
 id_checked, postpone, paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created)
VALUES ('200106974', '415240303', '415', 'Pamela.Collins', true, 13, 1, NULL, '415 4', NULL, NULL, NULL, NULL, '0081',
        NULL, '2024-04-08', false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-04-16 18:18:02.558', NULL, NULL,
        '2024-04-12 23:58:34.227');


INSERT INTO juror_mod.financial_audit_details
(id, juror_revision, court_location_revision, "type", created_by, created_on, juror_number, loc_code)
VALUES (91, 42257, 1, 'FOR_APPROVAL', 'test_court_standard', '2024-04-18 12:30:56.922', '200106974', '415'),
       (92, 42257, 1, 'FOR_APPROVAL', 'test_court_standard', '2024-04-19 12:31:06.447', '200106974', '415'),
       (93, 42257, 1, 'FOR_APPROVAL_EDIT', 'test_court_standard', '2024-04-20 12:31:54.736', '200106974', '415'),
       (94, 42257, 1, 'APPROVED_BACS', 'test_court_manager', '2024-04-21 12:32:26.614', '200106974', '415'),
       (95, 42257, 1, 'APPROVED_EDIT', 'test_court_standard', '2024-04-22 12:38:15.223', '200106974', '415'),
       (96, 42257, 1, 'REAPPROVED_BACS', 'test_court_manager', '2024-04-23 12:39:17.146', '200106974', '415');


INSERT INTO juror_mod.appearance
(attendance_date, juror_number, loc_code, time_in, time_out, trial_number, non_attendance, no_show, misc_description,
 pay_cash, last_updated_by, created_by, public_transport_total_due, public_transport_total_paid,
 hired_vehicle_total_due, hired_vehicle_total_paid, motorcycle_total_due, motorcycle_total_paid, car_total_due,
 car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid, childcare_total_due, childcare_total_paid,
 parking_total_due, parking_total_paid, misc_total_due, misc_total_paid, smart_card_due, is_draft_expense, f_audit,
 sat_on_jury, pool_number, appearance_stage, loss_of_earnings_due, loss_of_earnings_paid, subsistence_due,
 subsistence_paid, attendance_type, smart_card_paid, travel_time, travel_jurors_taken_by_car, travel_by_car,
 travel_jurors_taken_by_motorcycle, travel_by_motorcycle, travel_by_bicycle, miles_traveled, food_and_drink_claim_type,
 "version", expense_rates_id, attendance_audit_number)
VALUES ('2024-03-25', '200106974', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.78, 4.78, NULL, NULL, 10.00, 10.00, 0.00, 0.00, NULL, NULL, NULL, false, 96, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 5.71, 5.71, 'FULL_DAY', NULL, '00:00:00', 2, true, NULL, false,
        false, 12, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 28, 2, NULL),
       ('2024-04-01', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, NULL, 2, true, NULL, false,
        false, 11, NULL, 18, 2, NULL),
       ('2024-04-08', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, true,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 0.00, 0.00, 'HALF_DAY_LONG_TRIAL', NULL, '00:00:00', 2, true,
        NULL, false, false, 11, 'NONE', 24, 2, NULL),
       ('2024-03-28', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, NULL, 2, true, NULL, false,
        false, 11, NULL, 21, 2, NULL),
       ('2024-03-29', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, NULL, 2, true, NULL, false,
        false, 11, NULL, 22, 2, NULL),
       ('2024-04-02', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, NULL, 2, true, NULL, false,
        false, 11, NULL, 21, 2, NULL),
       ('2024-04-05', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 0.00, 0.00, 'HALF_DAY', NULL, '00:00:00', 2, true, NULL, false,
        false, 11, 'NONE', 23, 2, NULL),
       ('2024-04-03', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, NULL, 2, true, NULL, false,
        false, 11, NULL, 21, 2, NULL),
       ('2024-04-04', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, false, 94, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, NULL, 2, true, NULL, false,
        false, 11, NULL, 21, 2, NULL),
       ('2024-03-26', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.78, 4.78, NULL, NULL, 10.00, 10.00, 0.00, 0.00, NULL, NULL, NULL, false, 96, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 12.17, 12.17, 'FULL_DAY', NULL, '00:00:00', 2, true, NULL,
        false, false, 12, 'MORE_THAN_10_HOURS', 30, 2, NULL),
       ('2024-03-27', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, 4.78, 4.78, NULL, NULL, 10.00, 10.00, 0.00, 0.00, NULL, NULL, 1.00, false, 96, NULL,
        '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 0.00, 0.00, 'FULL_DAY', 1.00, '00:00:00', 2, true, NULL, false,
        false, 12, 'NONE', 30, 2, NULL);

INSERT INTO juror_mod.appearance_audit
(revision, rev_type, attendance_date, juror_number, loc_code, time_in, time_out, trial_number, non_attendance, no_show,
 misc_description, pay_cash, last_updated_by, created_by, public_transport_total_due, public_transport_total_paid,
 hired_vehicle_total_due, hired_vehicle_total_paid, motorcycle_total_due, motorcycle_total_paid, car_total_due,
 car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid, childcare_total_due, childcare_total_paid,
 parking_total_due, parking_total_paid, misc_total_due, misc_total_paid, smart_card_due, smart_card_paid,
 is_draft_expense, f_audit, sat_on_jury, pool_number, appearance_stage, loss_of_earnings_due, loss_of_earnings_paid,
 subsistence_due, subsistence_paid, attendance_type, travel_time, travel_jurors_taken_by_car, travel_by_car,
 travel_jurors_taken_by_motorcycle, travel_by_motorcycle, travel_by_bicycle, miles_traveled, food_and_drink_claim_type,
 expense_rates_id, attendance_audit_number, "version")
VALUES (105460, 1, '2024-03-25', '200106974', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 22),
       (105460, 1, '2024-04-01', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 15),
       (105460, 1, '2024-04-08', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, true, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY_LONG_TRIAL', NULL, 2, true,
        NULL, false, false, 11, NULL, 2, NULL, 18),
       (105460, 1, '2024-03-28', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 17),
       (105460, 1, '2024-03-29', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 18),
       (105460, 1, '2024-04-02', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 17),
       (105460, 1, '2024-04-05', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 17),
       (105460, 1, '2024-04-03', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 17),
       (105460, 1, '2024-04-04', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 17),
       (105460, 1, '2024-03-26', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 21),
       (105460, 1, '2024-03-27', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 21),
       (105461, 1, '2024-03-25', '200106974', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 5.71, NULL, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 2, NULL, 23),
       (105462, 1, '2024-03-26', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 12.17, NULL, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'MORE_THAN_10_HOURS', 2, NULL, 22),
       (105463, 1, '2024-03-27', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, 1.00, NULL,
        true, NULL, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 0.00, NULL, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'NONE', 2, NULL, 22),
       (105464, 1, '2024-03-25', '200106974', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 91, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 5.71, NULL, 'FULL_DAY', '00:00:00', 2, true, NULL,
        false, false, 11, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 2, NULL, 24),
       (105464, 1, '2024-03-28', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 91, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 19),
       (105464, 1, '2024-03-26', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 91, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 12.17, NULL, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'MORE_THAN_10_HOURS', 2, NULL, 24),
       (105464, 1, '2024-03-27', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, 1.00, NULL,
        false, 91, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 0.00, NULL, 'FULL_DAY', '00:00:00', 2, true, NULL,
        false, false, 11, 'NONE', 2, NULL, 24),
       (105465, 1, '2024-04-01', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 92, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 16),
       (105465, 1, '2024-04-08', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 92, true, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY_LONG_TRIAL', NULL, 2, true,
        NULL, false, false, 11, NULL, 2, NULL, 20),
       (105465, 1, '2024-03-29', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 92, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 20),
       (105465, 1, '2024-04-02', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 92, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 19),
       (105465, 1, '2024-04-05', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 92, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 19),
       (105465, 1, '2024-04-03', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 92, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 19),
       (105465, 1, '2024-04-04', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 92, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 19),
       (105466, 1, '2024-04-05', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 93, NULL, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 0.00, NULL, 'HALF_DAY', '00:00:00', 2, true, NULL,
        false, false, 11, 'NONE', 2, NULL, 21),
       (105466, 1, '2024-04-08', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, NULL, 0.00, NULL, NULL, NULL, 4.38, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL,
        false, 93, true, '415240303', 'EXPENSE_ENTERED', 22.00, NULL, 0.00, NULL, 'HALF_DAY_LONG_TRIAL', '00:00:00', 2,
        true, NULL, false, false, 11, 'NONE', 2, NULL, 22),
       (105469, 1, '2024-03-25', '200106974', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.78, 4.78, NULL, NULL, 10.00, 10.00, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 96, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 5.71, 5.71, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 12, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 2, NULL, 28),
       (105469, 1, '2024-03-26', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.78, 4.78, NULL, NULL, 10.00, 10.00, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 96, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 12.17, 12.17, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 12, 'MORE_THAN_10_HOURS', 2, NULL, 30),
       (105469, 1, '2024-03-27', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.78, 4.78, NULL, NULL, 10.00, 10.00, 0.00, 0.00, NULL, NULL, 1.00, 1.00,
        false, 96, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 0.00, 0.00, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 12, 'NONE', 2, NULL, 30),
       (105467, 1, '2024-03-25', '200106974', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 5.71, 5.71, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 2, NULL, 25),
       (105467, 1, '2024-04-01', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 18),
       (105467, 1, '2024-04-08', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, true, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 0.00, 0.00, 'HALF_DAY_LONG_TRIAL', '00:00:00',
        2, true, NULL, false, false, 11, 'NONE', 2, NULL, 24),
       (105467, 1, '2024-03-28', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 21),
       (105467, 1, '2024-03-29', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 22),
       (105467, 1, '2024-04-02', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 21),
       (105467, 1, '2024-04-05', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 0.00, 0.00, 'HALF_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'NONE', 2, NULL, 23),
       (105467, 1, '2024-04-03', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 21),
       (105467, 1, '2024-04-04', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, NULL, NULL, 'FULL_DAY', NULL, 2, true, NULL,
        false, false, 11, NULL, 2, NULL, 21),
       (105467, 1, '2024-03-26', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 12.17, 12.17, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'MORE_THAN_10_HOURS', 2, NULL, 26),
       (105467, 1, '2024-03-27', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.38, 4.38, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, 1.00, 1.00,
        false, 94, NULL, '415240303', 'EXPENSE_AUTHORISED', 22.00, 22.00, 0.00, 0.00, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 11, 'NONE', 2, NULL, 26),
       (105468, 1, '2024-03-25', '200106974', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.78, 4.38, NULL, NULL, 10.00, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 95, NULL, '415240303', 'EXPENSE_EDITED', 22.00, 22.00, 5.71, 5.71, 'FULL_DAY', '00:00:00', 2, true, NULL,
        false, false, 12, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 2, NULL, 27),
       (105468, 1, '2024-03-26', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.78, 4.38, NULL, NULL, 10.00, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL,
        false, 95, NULL, '415240303', 'EXPENSE_EDITED', 22.00, 22.00, 12.17, 12.17, 'FULL_DAY', '00:00:00', 2, true,
        NULL, false, false, 12, 'MORE_THAN_10_HOURS', 2, NULL, 28),
       (105468, 1, '2024-03-27', '200106974', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, 4.78, 4.38, NULL, NULL, 10.00, NULL, 0.00, 0.00, NULL, NULL, 1.00, 1.00,
        false, 95, NULL, '415240303', 'EXPENSE_EDITED', 22.00, 22.00, 0.00, 0.00, 'FULL_DAY', '00:00:00', 2, true, NULL,
        false, false, 12, 'NONE', 2, NULL, 28);


INSERT INTO juror_mod.financial_audit_details_appearances
(financial_audit_id, loc_code, attendance_date, appearance_version, last_approved_faudit)
VALUES (91, '415', '2024-03-25', 24, null),
       (91, '415', '2024-03-28', 19, null),
       (91, '415', '2024-03-26', 24, null),
       (91, '415', '2024-03-27', 24, null),
       (92, '415', '2024-04-01', 16, null),
       (92, '415', '2024-04-08', 20, null),
       (92, '415', '2024-03-29', 20, null),
       (92, '415', '2024-04-02', 19, null),
       (92, '415', '2024-04-05', 19, null),
       (92, '415', '2024-04-03', 19, null),
       (92, '415', '2024-04-04', 19, null),
       (93, '415', '2024-04-05', 21, null),
       (93, '415', '2024-04-08', 22, null),
       (94, '415', '2024-03-25', 25, null),
       (94, '415', '2024-04-01', 18, null),
       (94, '415', '2024-04-08', 24, null),
       (94, '415', '2024-03-28', 21, null),
       (94, '415', '2024-03-29', 22, null),
       (94, '415', '2024-04-02', 21, null),
       (94, '415', '2024-04-05', 23, null),
       (94, '415', '2024-04-03', 21, null),
       (94, '415', '2024-04-04', 21, null),
       (94, '415', '2024-03-26', 26, null),
       (94, '415', '2024-03-27', 26, null),
       (95, '415', '2024-03-25', 27, null),
       (95, '415', '2024-03-26', 28, null),
       (95, '415', '2024-03-27', 28, null),
       (96, '415', '2024-03-25', 28, 94),
       (96, '415', '2024-03-26', 30, 94),
       (96, '415', '2024-03-27', 30, 94);