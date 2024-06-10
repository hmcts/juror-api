
-- Dummy test data
insert into juror_mod.judge (id, owner, code, description)
values (1, '415', '1234', 'Test judge'),
       (2, '417', '4321', 'Judge Test');

insert into juror_mod.courtroom (id, loc_code, room_number, description)
values (1, '415', '1', 'large room fits 100 people'),
       (2, '417', '2', 'large room fits 100 people');

insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, trial_end_date,
                             anonymous, courtroom)
values ('T100000002', '415', 'TEST DEFENDANT', 1, 'CIV', current_date, null, false, 1),
       ('T100000003', '417', 'TEST DEFENDANT', 2, 'CIV', current_date, null, false, 2),
       ('T100000004', '415', 'TEST DEFENDANT', 1, 'CIV', current_date, null, false, 1),
       ('T100000005', '415', 'TEST DEFENDANT', 1, 'CIV', current_date, null, false, 1);



INSERT INTO juror_mod.users (username, "name", active, approval_limit, user_type,
                             email)
VALUES ('COURT.USER.270.415', 'COURT USER.270.415', true, 0.00, 'COURT',
        'COURT.USER.270.415@justice.gov.uk'),
       ('COURT.415', 'COURT 415', true, 100000.00, 'COURT', 'COURT.415@justice.gov.uk');

INSERT INTO juror_mod.juror
(juror_number, poll_number, title, last_name, first_name, dob, address_line_1, address_line_2, address_line_3,
 address_line_4, address_line_5, postcode, h_phone, w_phone, w_ph_local, responded, date_excused, excusal_code, acc_exc,
 date_disq, disq_code, user_edtq, notes, no_def_pos, perm_disqual, reasonable_adj_code, reasonable_adj_msg,
 smart_card_number, completion_date, sort_code, bank_acct_name, bank_acct_no, bldg_soc_roll_no, welsh, police_check,
 last_update, summons_file, m_phone, h_email, contact_preference, notifications, date_created, optic_reference,
 pending_title, pending_first_name, pending_last_name, mileage, financial_loss, travel_time, bureau_transfer_date,
 claiming_subsistence_allowance, service_comp_comms_status, login_attempts, is_locked)
VALUES ('200956973', NULL, 'Rev', 'Almoney', 'Alyce', '1978-05-31 00:00:00.000', '318 PADU Path', 'Room Number 689',
        'Stoke on Trent', 'Cumbria', NULL, 'AD1 5HG', NULL, NULL, NULL, true, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ELIGIBLE', '2024-05-09 17:44:53.000', NULL,
        NULL, NULL, NULL, 0, '2024-05-03 02:42:24.040', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 0,
        false),
       ('200959864', NULL, 'Rev', 'Latessa', 'Kristle', '1972-07-30 00:00:00.000', '9 ZNCV Square', 'Suite Number 486',
        'Colchester', 'Bedfordshire', NULL, 'AD1 2ZP', NULL, NULL, NULL, true, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ELIGIBLE', '2024-05-09 17:44:53.000', NULL,
        NULL, NULL, NULL, 0, '2024-05-03 02:42:24.373', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 0,
        false),
       ('200959899', NULL, 'Mr', 'Frank', 'Elbow', '1989-07-22 00:00:00.000', '3 Street Number', 'Suite Number 486',
        'Colchester', 'Bedfordshire', NULL, 'AD1 2ZP', NULL, NULL, NULL, true, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ELIGIBLE', '2024-05-09 17:44:53.000', NULL,
        NULL, NULL, NULL, 0, '2024-05-03 02:42:24.373', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 0,
        false);

INSERT INTO juror_mod.pool (pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update,
                            additional_summons, attend_time, nil_pool, total_no_required, date_created)
VALUES ('415240504', '415', '2024-05-06', 60, 'CRO', '415', 'N', '2024-05-03 02:42:25.000', NULL,
        '2024-05-06 08:30:00.000', false, 60, '2024-05-03 02:42:23.638'),
      ('417240504', '417', '2024-05-06', 60, 'CRO', '415', 'N', '2024-05-03 02:42:25.000', NULL,
        '2024-05-06 08:30:00.000', false, 60, '2024-05-03 02:42:23.638');

INSERT INTO juror_mod.juror_pool (juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date,
                                  "location", no_attendances, no_attended, no_fta, no_awol, pool_seq, edit_tag,
                                  next_date, on_call, smart_card, was_deferred, deferral_code, id_checked, postpone,
                                  paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created)
VALUES ('200956973', '415240504', '415', 'BURAU.USER36', true, 2, 1, NULL, '767', NULL, NULL, NULL, NULL, '0023',
        NULL, '2024-05-13', false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-05-11 03:47:17.199', NULL, NULL,
        '2024-05-03 02:42:24.042'),
       ('200959864', '415240504', '415', 'BURAU.USER72', true, 2, 1, NULL, '415', NULL, NULL, NULL, NULL, '0048',
        NULL, '2024-05-13', false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-05-11 21:56:05.096', NULL, NULL,
        '2024-05-03 02:42:24.376'),
       ('200959899', '417240504', '417', 'BURAU.USER72', true, 2, 1, NULL, '415', NULL, NULL, NULL, NULL, '0048',
        NULL, '2024-05-13', false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-05-11 21:56:05.096', NULL, NULL,
        '2024-05-03 02:42:24.376');


INSERT INTO juror_mod.expense_rates(id, rate_per_mile_car_0_passengers, rate_per_mile_car_1_passengers,
                                    rate_per_mile_car_2_or_more_passengers, rate_per_mile_motorcycle_0_passengers,
                                    rate_per_mile_motorcycle_1_or_more_passengers, rate_per_mile_bike,
                                    limit_financial_loss_half_day, limit_financial_loss_full_day,
                                    limit_financial_loss_half_day_long_trial, limit_financial_loss_full_day_long_trial,
                                    rate_subsistence_standard, rate_subsistence_long_day)
values (999998, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1),
       (999999, 0.314, 0.356, 0.398, 0.314, 0.324, 0.096, 32.47, 64.95, 64.95, 129.91, 5.71, 12.17);


INSERT INTO juror_mod.appearance (attendance_date, juror_number, loc_code, time_in, time_out, trial_number,
                                  non_attendance, no_show, misc_description, pay_cash, last_updated_by, created_by,
                                  public_transport_total_due, public_transport_total_paid, hired_vehicle_total_due,
                                  hired_vehicle_total_paid, motorcycle_total_due, motorcycle_total_paid, car_total_due,
                                  car_total_paid, pedal_cycle_total_due, pedal_cycle_total_paid, childcare_total_due,
                                  childcare_total_paid, parking_total_due, parking_total_paid, misc_total_due,
                                  misc_total_paid, smart_card_due, is_draft_expense, f_audit, sat_on_jury, pool_number,
                                  appearance_stage, loss_of_earnings_due, loss_of_earnings_paid, subsistence_due,
                                  subsistence_paid, attendance_type, smart_card_paid, travel_time,
                                  travel_jurors_taken_by_car, travel_by_car, travel_jurors_taken_by_motorcycle,
                                  travel_by_motorcycle, travel_by_bicycle, miles_traveled, food_and_drink_claim_type,
                                  "version", expense_rates_id, attendance_audit_number)
VALUES ('2024-05-06', '200956973', '415', '08:30:00', '16:30:00', 'T100000002', false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 2.00, 2.00, NULL, NULL, 0.63, 0.63, NULL, NULL, 2.00, 2.00, 5.00, 5.00, NULL, NULL, NULL, false, 12,
        NULL, '415240504', 'EXPENSE_AUTHORISED', 10.00, 10.00, 0.00, 0.00, 'FULL_DAY', NULL, '00:00:00', NULL, true,
        NULL, false, false, 2, 'NONE', 19, 999998, 'P10011777'),
       ('2024-05-07', '200956973', '415', '08:30:00', '16:30:00', 'T100000002', false, NULL, '', true, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, 4.00, 4.00, 4.00, 4.00, 2.00, 2.00, NULL, false, 16, NULL,
        '415240504', 'EXPENSE_AUTHORISED', 10.00, 10.00, 0.00, 0.00, 'FULL_DAY', NULL, '00:00:00', NULL, false, NULL,
        false, false, NULL, 'NONE', 21, 999998, 'P10012682'),
       ('2024-05-08', '200956973', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        3.00, 3.00, NULL, NULL, NULL, NULL, NULL, NULL, 2.00, 2.00, 0.00, 0.00, NULL, NULL, NULL, false, 12, NULL,
        '415240504', 'EXPENSE_AUTHORISED', 10.00, 10.00, 5.71, 5.71, 'FULL_DAY', NULL, '00:00:00', NULL, false, NULL,
        false, false, NULL, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 15, 999998, 'P10013503'),
       ('2024-05-09', '200956973', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        4.00, 4.00, NULL, NULL, NULL, NULL, NULL, NULL, 2.00, 2.00, 0.00, 0.00, NULL, NULL, NULL, false, 10, NULL,
        '415240504', 'EXPENSE_AUTHORISED', 10.00, 10.00, 0.00, 0.00, 'FULL_DAY', NULL, '00:00:00', NULL, false, NULL,
        false, false, NULL, 'NONE', 11, 999998, 'P10014275'),
       ('2024-05-10', '200956973', '415', '08:30:00', '16:30:00', 'T100000004', false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, 2.00, 2.00, 0.00, 0.00, 1.00, 1.00, NULL, false, 10, NULL,
        '415240504', 'EXPENSE_AUTHORISED', 10.00, 10.00, 0.00, 0.00, 'FULL_DAY', NULL, '00:00:00', NULL, false, NULL,
        false, false, NULL, 'NONE', 11, 999998, 'P10014995'),
       ('2024-05-13', '200956973', '415', '08:30:00', '16:30:00', 'T100000004', false, NULL, NULL, false, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2.00, 2.00, NULL, NULL, NULL, NULL, NULL, false, 10, true,
        '415240504', 'EXPENSE_AUTHORISED', 10.00, 10.00, NULL, NULL, 'FULL_DAY', NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, 12, 999998, 'P10016300'),
       ('2024-05-06', '200959864', '415', '08:30:00', '16:30:00', null, false, NULL, '', false, NULL, NULL,
        0.00, 0.00, 0.00, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00, 3.00, 3.00, 2.45, false, 20,
        NULL, '415240504', 'EXPENSE_AUTHORISED', NULL, NULL, 5.71, 5.71, 'FULL_DAY', 2.45, '00:00:00', NULL, false,
        NULL, false, false, NULL, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 15, 999998, 'P10011777'),
       ('2024-05-07', '200959864', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00, 3.00, 3.00, 2.34, false, 18, NULL,
        '415240504', 'EXPENSE_AUTHORISED', NULL, NULL, 5.71, 5.71, 'FULL_DAY', 2.34, '00:00:00', NULL, false, NULL,
        false, false, NULL, 'LESS_THAN_OR_EQUAL_TO_10_HOURS', 11, 999998, 'P10012682'),
       ('2024-05-08', '200959864', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, 0.00,
        0.00, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00, 3.00, 3.00, NULL, false, 18, NULL,
        '415240504', 'EXPENSE_AUTHORISED', NULL, NULL, 0.00, 0.00, 'FULL_DAY', NULL, '00:00:00', NULL, false, NULL,
        false, false, NULL, 'NONE', 11, 999998, 'P10013503'),
       ('2024-05-09', '200959864', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, NULL,
        0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, 3.00, NULL, NULL, true, NULL, NULL,
        '415240504', 'EXPENSE_ENTERED', NULL, NULL, NULL, NULL, 'FULL_DAY', NULL, NULL, NULL, false, NULL, false, false,
        NULL, NULL, 6, 999998, 'P10014275'),
       ('2024-05-10', '200959864', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, NULL,
        0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, 3.00, NULL, NULL, true, NULL, NULL,
        '415240504', 'EXPENSE_ENTERED', NULL, NULL, NULL, NULL, 'FULL_DAY', NULL, NULL, NULL, false, NULL, false, false,
        NULL, NULL, 6, 999998, 'P10014995'),
       ('2024-05-13', '200959864', '415', '08:30:00', '16:30:00', NULL, false, NULL, '', false, NULL, NULL, 0.00, NULL,
        0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, 3.00, NULL, NULL, true, NULL, true,
        '415240504', 'EXPENSE_ENTERED', NULL, NULL, NULL, NULL, 'FULL_DAY', NULL, NULL, NULL, false, NULL, false, false,
        NULL, NULL, 8, 999998, 'P10016300');

INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp)
VALUES (7344726, 1715794422606),
       (7344725, 1715794418757),
       (7344724, 1715794330912),
       (7344723, 1715794328058),
       (7344722, 1715794320350),
       (7344721, 1715794318531),
       (7344720, 1715794313449),
       (7344719, 1715794292833),
       (7344718, 1715794289889),
       (7344717, 1715794198624),
       (7344716, 1715794195608),
       (7344715, 1715794159322),
       (7344714, 1715794153859),
       (7344713, 1715793306383),
       (7344712, 1715793242177),
       (7344711, 1715706797710),
       (7344710, 1715706797710),
       (314446, 1715090205624),
       (1, 1714608000),
       (314491, 1715090206181);

INSERT INTO juror_mod.financial_audit_details (id, juror_revision, court_location_revision, "type", created_by,
                                               created_on, juror_number, loc_code)
VALUES (9, 314446, 1, 'FOR_APPROVAL', 'COURT.USER.270.415', '2024-05-14 18:13:04.224', '200956973', '415'),
       (10, 314446, 1, 'APPROVED_BACS', 'COURT.415', '2024-05-14 18:13:17.637', '200956973', '415'),
       (11, 314446, 1, 'APPROVED_EDIT', 'COURT.USER.270.415', '2024-05-15 18:14:02.133', '200956973', '415'),
       (12, 314446, 1, 'REAPPROVED_BACS', 'COURT.415', '2024-05-15 18:15:06.147', '200956973', '415'),
       (13, 314446, 1, 'APPROVED_EDIT', 'COURT.USER.270.415', '2024-05-15 18:29:13.815', '200956973', '415'),
       (14, 314446, 1, 'REAPPROVED_CASH', 'COURT.415', '2024-05-15 18:29:19.287', '200956973', '415'),
       (15, 314446, 1, 'APPROVED_EDIT', 'COURT.USER.270.415', '2024-05-15 18:29:55.548', '200956973', '415'),
       (16, 314446, 1, 'REAPPROVED_CASH', 'COURT.415', '2024-05-15 18:29:58.575', '200956973', '415'),
       (17, 314491, 1, 'FOR_APPROVAL', 'COURT.USER.270.415', '2024-05-15 18:32:08.007', '200959864', '415'),
       (18, 314491, 1, 'APPROVED_BACS', 'COURT.415', '2024-05-15 18:32:10.882', '200959864', '415'),
       (19, 314491, 1, 'APPROVED_EDIT', 'COURT.USER.270.415', '2024-05-15 18:33:38.742', '200959864', '415'),
       (20, 314491, 1, 'REAPPROVED_BACS', 'COURT.415', '2024-05-15 18:33:42.584', '200959864', '415');


INSERT INTO juror_mod.financial_audit_details_appearances (financial_audit_id, attendance_date, appearance_version,
                                                           loc_code, last_approved_faudit)
VALUES (9, '2024-05-06', 14, '415', NULL),
       (9, '2024-05-07', 9, '415', NULL),
       (9, '2024-05-08', 9, '415', NULL),
       (9, '2024-05-09', 9, '415', NULL),
       (9, '2024-05-10', 9, '415', NULL),
       (9, '2024-05-13', 10, '415', NULL),
       (10, '2024-05-06', 15, '415', NULL),
       (10, '2024-05-07', 11, '415', NULL),
       (10, '2024-05-08', 11, '415', NULL),
       (10, '2024-05-09', 11, '415', NULL),
       (10, '2024-05-10', 11, '415', NULL),
       (10, '2024-05-13', 12, '415', NULL),
       (11, '2024-05-06', 17, '415', NULL),
       (11, '2024-05-07', 13, '415', NULL),
       (11, '2024-05-08', 13, '415', NULL),
       (12, '2024-05-06', 19, '415', 10),
       (12, '2024-05-07', 15, '415', 10),
       (12, '2024-05-08', 15, '415', 10),
       (13, '2024-05-07', 16, '415', NULL),
       (14, '2024-05-07', 18, '415', 12),
       (15, '2024-05-07', 19, '415', NULL),
       (16, '2024-05-07', 21, '415', 14),
       (17, '2024-05-06', 11, '415', NULL),
       (17, '2024-05-07', 9, '415', NULL),
       (17, '2024-05-08', 9, '415', NULL),
       (18, '2024-05-06', 12, '415', NULL),
       (18, '2024-05-07', 11, '415', NULL),
       (18, '2024-05-08', 11, '415', NULL),
       (19, '2024-05-06', 13, '415', NULL),
       (20, '2024-05-06', 15, '415', 18);

