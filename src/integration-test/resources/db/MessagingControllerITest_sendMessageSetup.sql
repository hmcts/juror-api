INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER, FIRST_NAME, LAST_NAME, DOB, postcode, h_email, h_phone, RESPONDED,
                             completion_date, address_line_1, address_line_4, welsh)
VALUES ('100000001', 'FNAME01', 'LNAME01', '2000-01-01', 'CH01 1AN', 'FNAME01.LNAME01@email.com', '07777000001', 'Y',
        null, '540 STREET NAME', 'ANYTOWN', true),
       ('100000002', 'FNAME02', 'LNAME02', '2000-01-02', 'CH02 1AN', 'FNAME02.LNAME02@email.com', '07777000002', 'Y',
        null, '540 STREET NAME', 'ANYTOWN', false),

       ('100000004', 'FNAME04', 'LNAME04', '2000-01-04', 'CH04 1AN', 'FNAME04.LNAME04@email.com', '07777000004', 'Y',
        null, '540 STREET NAME', 'ANYTOWN', false),
       ('100000014', 'FNAME14', 'LNAME14', '2000-01-14', 'CH14 1AN', 'FNAME14.LNAME14@email.com', '07777000014', 'Y',
        null, '540 STREET NAME', 'ANYTOWN', false),


       ('100000015', 'FNAME14', 'LNAME14', '2000-01-14', 'CH14 1AN', 'FNAME14.LNAME14@email.com', '07777000014', 'Y',
        null, '540 STREET NAME', 'ANYTOWN', false),

       ('100000016', 'FNAME14', 'LNAME14', '2000-01-14', 'CH14 1AN', 'FNAME14.LNAME14@email.com', null, 'Y',
        null, '540 STREET NAME', 'ANYTOWN', false),

       ('100000017', 'FNAME14', 'LNAME14', '2000-01-14', 'CH14 1AN', null, '07777000014', 'Y',
        null, '540 STREET NAME', 'ANYTOWN', false)

;


INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE,
                            NEW_REQUEST, LAST_UPDATE, ADDITIONAL_SUMMONS, ATTEND_TIME)
VALUES ('462', '200000002', TIMESTAMP'2022-09-04 00:00:00.0', 5, 5, 'CRO', '462', 'Y', TIMESTAMP'2022-02-02 09:22:09.0',
        NULL, TIMESTAMP'2022-09-05 09:00:00.0'),
       ('462', '200000014', TIMESTAMP'2022-09-04 00:00:00.0', 5, 5, 'CRO', '462', 'Y', TIMESTAMP'2022-02-02 09:22:09.0',
        NULL, TIMESTAMP'2022-09-07 09:00:00.0'),
       ('756', '200000015', TIMESTAMP'2022-09-04 00:00:00.0', 5, 5, 'CRO', '756', 'Y', TIMESTAMP'2022-02-02 09:22:09.0',
        NULL, TIMESTAMP'2022-09-08 09:00:00.0');

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, next_date, def_date, on_call, STATUS, IS_ACTIVE)
VALUES ('756', '100000001', '200000015', '2022-01-01', null, false, 4, TRUE),
       ('462', '100000002', '200000002', '2022-01-02', null, false, 4, TRUE),
       ('462', '100000004', '200000014', '2022-01-04', null, true, 4, TRUE),
       ('756', '100000014', '200000015', '2022-01-14', null, false, 4, TRUE),
       ('462', '100000015', '200000014', '2022-01-04', null, true, 12, TRUE);


-- Reset sequence numbers
SELECT setval('juror_mod.courtroom_id_seq', 65);
SELECT setval('juror_mod.judge_id_seq', 20);

-- Dummy test data
insert into juror_mod.judge (owner, code, description)
values ('462', '1234', 'Test judge'),
       ('756', '4321', 'Judge Test');

insert into juror_mod.courtroom (loc_code, room_number, description)
values ('462', '1', 'large room fits 100 people'),
       ('756', '2', 'large room fits 100 people');

insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, trial_end_date,
                             anonymous, courtroom)
values ('T100000002', '462', 'TEST DEFENDANT', 21, 'CIV', current_date, null, false, 66),
       ('T100000003', '756', 'TEST DEFENDANT', 21, 'CIV', current_date, null, false, 66),
       ('T100000004', '462', 'TEST DEFENDANT', 21, 'CIV', current_date, null, false, 66);

INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, rand_number, date_selected, "result", completed)
VALUES
    --Status = Panel (3) or Juror(4) and result is J
    ('756', '100000001', 'T100000003', 1, '2023-11-30 13:50:58.821', 'J', false),
    ('462', '100000002', 'T100000002', 2, '2023-11-30 13:50:58.821', 'J', false),
    ('462', '100000004', 'T100000002', 3, '2023-11-30 13:50:59.110', 'J', false),
    ('756', '100000014', 'T100000003', 4, '2023-11-30 13:50:59.110', 'J', false);
