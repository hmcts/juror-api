DELETE FROM juror_mod.app_setting WHERE setting IN (
    'WE_ARE_GROUP_CONTACT_INFORMATION_TEMPLATE_ID',
    'WE_ARE_GROUP_REFERRAL_CONFIRMED_TEMPLATE_ID'
);

INSERT INTO juror_mod.app_setting (setting, value)
VALUES ('WE_ARE_GROUP_CONTACT_INFORMATION_TEMPLATE_ID', 'eda6bed7-3e34-46d0-9f28-5c0fd0706e75'),
       ('WE_ARE_GROUP_REFERRAL_CONFIRMED_TEMPLATE_ID', 'a2cbeed3-4121-4fcd-b9d4-1de2f7b5c0c2');

INSERT INTO juror_mod.juror (juror_number, poll_number, title, last_name, first_name, dob, address_line_1,
                             address_line_2, address_line_3, address_line_4, address_line_5, postcode, h_phone, w_phone,
                             w_ph_local, responded, date_excused, excusal_code, acc_exc, date_disq, disq_code,
                             user_edtq, notes, no_def_pos, perm_disqual, reasonable_adj_code, reasonable_adj_msg,
                             smart_card_number, completion_date, sort_code, bank_acct_name, bank_acct_no,
                             bldg_soc_roll_no, welsh, police_check, last_update, summons_file, m_phone, h_email,
                             contact_preference, notifications, date_created, optic_reference, pending_title,
                             pending_first_name, pending_last_name, mileage, financial_loss, travel_time,
                             bureau_transfer_date, claiming_subsistence_allowance, service_comp_comms_status,
                             login_attempts, is_locked)
VALUES ('610000050', '0', 'Mr', 'Juror', 'Test', '1985-03-23 00:00:00', '1 Test Street', 'Wales', 'Wrexham',
        'United Kingdom', NULL, 'LL130BH', '01244111111', NULL, NULL, false, NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, 'NOT_CHECKED', '2024-03-11 23:22:11',
        NULL, NULL, 'test.juror@example.com', 0, 0, '2024-03-11 23:22:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, false, NULL, 0, false);
