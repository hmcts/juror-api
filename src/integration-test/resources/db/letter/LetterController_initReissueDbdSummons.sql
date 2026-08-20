UPDATE juror_mod.court_location
SET digital_by_default = true
WHERE loc_code IN ('415', '774');

INSERT INTO juror_mod.pool (pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request,
                            last_update, additional_summons, attend_time, nil_pool, total_no_required, date_created)
VALUES ('415241001', '400', DATE '2024-10-08', 1, 'CRO', '415', 'N', CURRENT_DATE, NULL, NULL, false, 1, NULL),
       ('774241001', '400', DATE '2024-10-08', 1, 'CRO', '774', 'N', CURRENT_DATE, NULL, NULL, false, 1, NULL);

INSERT INTO juror_mod.juror (juror_number, poll_number, title, last_name, first_name, dob, address_line_1,
                             address_line_2, address_line_3, address_line_4, address_line_5, postcode, h_phone,
                             w_phone, w_ph_local, responded, date_excused, excusal_code, acc_exc, date_disq,
                             disq_code, user_edtq, notes, no_def_pos, perm_disqual, reasonable_adj_code,
                             reasonable_adj_msg, smart_card_number, completion_date, sort_code, bank_acct_name,
                             bank_acct_no, bldg_soc_roll_no, welsh, police_check, last_update, summons_file, m_phone,
                             h_email, contact_preference, notifications, date_created, optic_reference, pending_title,
                             pending_first_name, pending_last_name, travel_time, mileage, financial_loss)
VALUES ('555555561', '540', 'Mr', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1998-03-08 00:00:00.000',
        'Address Line 1', 'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', NULL, NULL, NULL,
        false, NULL, NULL, NULL, NULL, NULL, 'BUREAU_USER', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, false, 'NOT_CHECKED', '2024-01-16 12:07:42.000', NULL, NULL, 'juror.one@example.test', 0, 0, NULL,
        '12345678', 'Mr', 'Test', 'Person', NULL, NULL, NULL),
       ('555555562', '541', 'Mrs', 'LNAMEFIVEFOURONE', 'FNAMEFIVEFOURONE', '1998-03-08 00:00:00.000',
        'Address Line 1', 'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CF10 1AA', NULL, NULL, NULL,
        false, NULL, NULL, NULL, NULL, NULL, 'BUREAU_USER', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, true, 'NOT_CHECKED', '2024-01-16 12:07:42.000', NULL, NULL, 'juror.two@example.test', 0, 0, NULL,
        '12345679', 'Mrs', 'Test', 'Person', NULL, NULL, NULL);

UPDATE juror_mod.juror
SET digital_by_default = true,
    dbd_preference = 'Digital'
WHERE juror_number IN ('555555561', '555555562');

INSERT INTO juror_mod.juror_pool (juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel,
                                  def_date, "location", no_attendances, no_attended, no_fta, no_awol, pool_seq,
                                  edit_tag, next_date, on_call, smart_card, was_deferred, deferral_code, id_checked,
                                  postpone, paid_cash, scan_code, last_update, reminder_sent, transfer_date,
                                  date_created)
VALUES ('555555561', '415241001', '400', 'BUREAU_USER', true, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0109',
        NULL, DATE '2024-10-08', false, NULL, NULL, 'A', NULL, NULL, NULL, NULL, '2024-01-16 12:07:42.162505',
        NULL, NULL, NULL),
       ('555555562', '774241001', '400', 'BUREAU_USER', true, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0110',
        NULL, DATE '2024-10-08', false, NULL, NULL, 'A', NULL, NULL, NULL, NULL, '2024-01-16 12:07:42.162505',
        NULL, NULL, NULL);

INSERT INTO juror_mod.bulk_print_data (juror_no, creation_date, form_type, detail_rec, extracted_flag, digital_comms)
VALUES ('555555561', CURRENT_DATE - 1, '6220', RPAD('415241001', 610, ' '), true, true),
       ('555555562', CURRENT_DATE - 1, '6220C', RPAD('774241001', 873, ' '), true, true);
