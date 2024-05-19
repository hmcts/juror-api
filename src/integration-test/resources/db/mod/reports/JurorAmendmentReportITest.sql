INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp)
VALUES (7344729, 1715794422606),
       (7344730, 1715794418757),
       (7344731, 1715794330912),
       (7344732, 1715794328058),
       (7344733, 1715794320350),
       (7344734, 1715794318531),
       (7344735, 1715794313449),
       (7344736, 1715794313449),
       (7344737, 1715794313449);

INSERT INTO juror_mod.users (username, "name", active, last_logged_in, "version", team_id, approval_limit,
                             user_type, email, created_by, updated_by)
VALUES ('COURT.469', 'COURT 469', true, NULL, 1, NULL, 100000.00, 'COURT', 'COURT.469@justice.gov.uk', NULL,
        NULL),
       ('COURT.469.2', 'COURT 469 2', true, NULL, 1, NULL, 100000.00, 'COURT', 'COURT.469.2@justice.gov.uk', NULL,
        NULL);

INSERT INTO juror_mod.pool (pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update,
                            additional_summons, attend_time, nil_pool, total_no_required, date_created)
VALUES ('469240419', '469', '2024-04-22', 60, 'CRO', '469', 'N', '2024-05-03 19:39:26.000', NULL,
        '2024-04-22 08:30:00.000', false, 60, '2024-05-03 19:39:24.527');

INSERT INTO juror_mod.juror (juror_number, poll_number, title, last_name, first_name, dob, address_line_1,
                             address_line_2, address_line_3, address_line_4, address_line_5, postcode, h_phone, w_phone,
                             w_ph_local, responded, date_excused, excusal_code, acc_exc, date_disq, disq_code,
                             user_edtq, notes, no_def_pos, perm_disqual, reasonable_adj_code, reasonable_adj_msg,
                             smart_card_number, completion_date, sort_code, bank_acct_name, bank_acct_no,
                             bldg_soc_roll_no, welsh, police_check, last_update, summons_file, m_phone, h_email,
                             contact_preference, notifications, date_created, optic_reference, pending_title,
                             pending_first_name, pending_last_name, mileage, financial_loss, travel_time,
                             bureau_transfer_date, claiming_subsistence_allowance, service_comp_comms_status,
                             login_attempts, is_locked, last_modified_by)
VALUES ('200160029', NULL, 'Ms', 'Adeniran', 'Norma3', '1996-08-26 00:00:00.000', ' AIRLL Nook 3', 'Box Number 74',
        'Swansea', 'Special post town 2', '', 'BD2 7BN', '011111111111', NULL, NULL, true, NULL, NULL, NULL, NULL, NULL,
        'COURT.469', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '123456', 'Test Name 2', '12345678', NULL, NULL,
        'ELIGIBLE', '2024-05-19 17:20:03.000', NULL, NULL, 'Norma.Adeniran@email.coam', NULL, 0,
        '2024-05-03 19:39:25.130', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 0, false, 'COURT.469');

INSERT INTO juror_mod.juror_pool (juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date,
                                  "location", no_attendances, no_attended, no_fta, no_awol, pool_seq, edit_tag,
                                  next_date, on_call, smart_card, was_deferred, deferral_code, id_checked, postpone,
                                  paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created)
VALUES ('200160029', '469240419', '469', 'BURAU.USER2', true, 2, 1, NULL, '469 8', NULL, NULL, NULL, NULL, '0040', NULL,
        '2024-05-13', false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-05-12 04:42:43.078', NULL, NULL,
        '2024-05-03 19:39:25.133');

INSERT INTO juror_mod.juror_audit (revision, juror_number, rev_type, title, first_name, last_name, dob, address_line_1,
                                   address_line_2, address_line_3, address_line_4, address_line_5, address6, postcode,
                                   h_email, h_phone, m_phone, w_phone, w_ph_local, bank_acct_name, bank_acct_no,
                                   bldg_soc_roll_no, sort_code, pending_title, pending_first_name, pending_last_name,
                                   claiming_subsistence_allowance, smart_card_number, last_modified_by, last_update)
VALUES (7344736, '200160029', 1, 'Ms', 'Norma3', 'Adeniran', '1996-08-26', ' AIRLL Nook 3', 'Box Number 74', 'Swansea',
        'Special post town 2', '', NULL, 'BD2 7BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL,
        'Test Name', '30459873', NULL, '608407', NULL, NULL, NULL, false, NULL, 'COURT.469', '2024-05-22 17:19:52.587'),
       (7344737, '200160029', 1, 'Ms', 'Norma3', 'Adeniran', '1996-08-26', ' AIRLL Nook 3', 'Box Number 74', 'Swansea',
        'Special post town 2', '', NULL, 'BD2 7BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL,
        'Test Name 2', '12345678', NULL, '123456', NULL, NULL, NULL, false, NULL, 'COURT.469',
        '2024-05-22 17:20:02.677'),
       (7344729, '200160029', 1, 'Ms', 'Norma', 'Adeniran', '1997-08-17', ' AIRLL Nook', 'Box Number 74', 'Swansea',
        'Special post town', '', NULL, 'BD2 9BN', 'Norma.Adeniran@email.coam', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, false, NULL, 'COURT.469', '2024-05-21 13:56:53.630'),
       (7344730, '200160029', 1, 'Ms', 'Norma', 'Adeniran', '1997-08-26', ' AIRLL Nook', 'Box Number 74', 'Swansea',
        'Special post town', '', NULL, 'BD2 9BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 'COURT.469', '2024-05-20 14:07:38.620'),
       (7344731, '200160029', 1, 'Ms', 'Norma2', 'Adeniran', '1997-08-26', ' AIRLL Nook', 'Box Number 74', 'Swansea',
        'Special post town', '', NULL, 'BD2 9BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 'COURT.469', '2024-05-20 14:09:16.638'),
       (7344732, '200160029', 1, 'Ms', 'Norma2', 'Adeniran', '1997-08-26', ' AIRLL Nook', 'Box Number 74', 'Swansea',
        'Special post town 2', '', NULL, 'BD2 7BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 'COURT.469.2', '2024-05-19 14:09:16.695'),
       (7344733, '200160029', 1, 'Ms', 'Norma2', 'Adeniran', '1997-08-26', ' AIRLL Nook 3', 'Box Number 74', 'Swansea',
        'Special post town 2', '', NULL, 'BD2 7BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 'COURT.469', '2024-05-19 14:09:31.692'),
       (7344734, '200160029', 1, 'Ms', 'Norma3', 'Adeniran', '1997-08-26', ' AIRLL Nook 3', 'Box Number 74', 'Swansea',
        'Special post town 2', '', NULL, 'BD2 7BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 'COURT.469', '2024-05-18 14:09:38.457'),
       (7344735, '200160029', 1, 'Ms', 'Norma3', 'Adeniran', '1996-08-26', ' AIRLL Nook 3', 'Box Number 74', 'Swansea',
        'Special post town 2', '', NULL, 'BD2 7BN', 'Norma.Adeniran@email.coam', '011111111111', NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 'COURT.469', '2024-05-18 14:09:45.443');