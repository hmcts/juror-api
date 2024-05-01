-- staff
INSERT INTO juror_mod.users (username,email, name, active, team_id, version)
VALUES ('JPOWERS','JPOWERS@email.gov.uk','Joanna Powers', true, 1, 0),
       ('TSANCHEZ','TSANCHEZ@email.gov.uk','Todd Sanchez', true, 2, 0),
       ('GBECK','GBECK@email.gov.uk','Grant Beck', false, 3, 0),
       ('RPRICE','RPRICE@email.gov.uk','Roxanne Price', true, 1, 0),
       ('PBREWER','PBREWER@email.gov.uk','Preston Brewer', true, 2, 0),
       ('ACOPELAND','ACOPELAND@email.gov.uk','Amelia Copeland', true, 3, 0);

insert into juror_mod.user_courts (username, loc_code)
values ('JPOWERS', '446'),
       ('TSANCHEZ', '446'),
       ('GBECK', '446'),
       ('RPRICE', '446'),
       ('PBREWER', '626'),
       ('ACOPELAND', '400');

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('RPRICE', 'MANAGER'),
       ('PBREWER', 'MANAGER'),
       ('ACOPELAND', 'MANAGER');

INSERT INTO juror_mod.pool (pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update,
                            additional_summons, attend_time, nil_pool, total_no_required, date_created)
VALUES ('555', '400', '2022-05-03', 5, 'CRO', '446', 'N', NULL, NULL, NULL, false, 5, NULL);

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
VALUES ('123251234', '21112', 'Mr', 'Hoola', 'Gypsey', '1984-07-24 00:00:00', '27 Knutson Trail', 'Scotland',
        'Aberdeen', 'United Kingdom', NULL, 'AB21 3RY', '44(703)209-6991', '44(109)549-5621', NULL, false, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, 'NOT_CHECKED',
        '2024-03-13 00:53:36', NULL, '44(145)525-2391', 'jhoola@ed.gov', 0, 0, '2024-03-13 00:53:36', NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, false, NULL, 0, false),
       ('209092530', '21112', 'Dr', 'Castillo', 'Jane', '1984-07-24 00:00:00', '4 Knutson Trail', 'Scotland',
        'Aberdeen', 'United Kingdom', NULL, 'AB21 3RY', '44(703)209-6993', '44(109)549-5625', NULL, false, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, 'NOT_CHECKED',
        '2024-03-13 00:53:37', NULL, '44(145)525-2390', 'jcastillo0@ed.gov', 0, 0, '2024-03-13 00:53:37', NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 0, false);
INSERT INTO juror_mod.juror_pool (juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date,
                                  "location", no_attendances, no_attended, no_fta, no_awol, pool_seq, edit_tag,
                                  next_date, on_call, smart_card, was_deferred, deferral_code, id_checked, postpone,
                                  paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created)
VALUES ('123251234', '555', '400', NULL, true, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        current_date + 60, false, NULL, false, NULL, NULL, false, false, NULL, '2024-03-13 00:53:36', false, NULL,
        NULL),
       ('209092530', '555', '400', NULL, true, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        current_date + 60, false, NULL, false, NULL, NULL, false, false, NULL, '2024-03-13 00:53:37', false, NULL,
        NULL);
INSERT INTO juror_mod.juror_response (juror_number, date_received, title, first_name, last_name, address_line_1,
                                      address_line_2, address_line_3, address_line_4, address_line_5, postcode,
                                      processing_status, date_of_birth, phone_number, alt_phone_number, email,
                                      residency, residency_detail, mental_health_act, mental_health_capacity,
                                      mental_health_act_details, bail, bail_details, convictions, convictions_details,
                                      deferral, deferral_reason, deferral_date, reasonable_adjustments_arrangements,
                                      excusal, excusal_reason, processing_complete, signed, "version", thirdparty_fname,
                                      thirdparty_lname, relationship, main_phone, other_phone, email_address,
                                      thirdparty_reason, thirdparty_other_reason, juror_phone_details,
                                      juror_email_details, staff_login, staff_assignment_date, urgent, super_urgent,
                                      completed_at, welsh, reply_type)
VALUES ('123251234', '2024-03-13 00:00:00', 'Mr', 'Gypsey', 'Hoola', '27 Knutson Trail', 'Scotland', 'Aberdeen',
        'United Kingdom', NULL, 'AB21 3RY', 'TODO', '1984-07-24', '44(703)209-6991', '44(145)525-2391', 'jhoola@ed.gov',
        true, NULL, false, NULL, NULL, false, NULL, false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, 0,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, false, NULL, NULL, true, false, NULL, false, 'Digital'),
       ('209092530', '2024-03-13 00:00:00', 'Dr', 'Jane', 'Castillo', '4 Knutson Trail', 'Scotland', 'Aberdeen',
        'United Kingdom', NULL, 'AB21 3RY', 'TODO', '1984-07-24', '44(703)209-6993', '44(145)525-2390',
        'jcastillo0@ed.gov', true, NULL, false, NULL, NULL, false, NULL, false, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, false, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, false, NULL, NULL, false, true,
        NULL, false, 'Digital');
UPDATE juror_mod.JUROR_RESPONSE
SET TITLE             = 'Mr',
    FIRST_NAME        = 'Gypsey',
    LAST_NAME         = 'Hoola',
    address_line_1    = '27 Knutson Trail',
    address_line_2    = 'Scotland',
    address_line_3    = 'Aberdeen',
    address_line_4    = 'United Kingdom',
    postcode          = 'AB21 3RY',
    PROCESSING_STATUS = 'TODO',
    DATE_OF_BIRTH     = TO_DATE('1984-07-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    PHONE_NUMBER      = '44(703)209-6991',
    ALT_PHONE_NUMBER  = '44(145)525-2391',
    EMAIL             = 'jhoola@ed.gov',
    RESIDENCY='Y',
    URGENT='Y',
    SUPER_URGENT='N'
WHERE JUROR_NUMBER = '123251234';

UPDATE juror_mod.JUROR_RESPONSE
SET TITLE             = 'Dr',
    FIRST_NAME        = 'Jane',
    LAST_NAME         = 'Castillo',
    address_line_1    = '4 Knutson Trail',
    address_line_2    = 'Scotland',
    address_line_3    = 'Aberdeen',
    address_line_4    = 'United Kingdom',
    postcode          = 'AB21 3RY',
    PROCESSING_STATUS = 'TODO',
    DATE_OF_BIRTH     = TO_DATE('1984-07-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    PHONE_NUMBER      = '44(703)209-6993',
    ALT_PHONE_NUMBER  = '44(145)525-2390',
    EMAIL             = 'jcastillo0@ed.gov',
    RESIDENCY='Y',
    URGENT='N',
    SUPER_URGENT='Y'

WHERE JUROR_NUMBER = '209092530';
