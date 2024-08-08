-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '415230102', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230103', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),

       ('414', '414230101', '2023-01-05', 5, 5, 'CRO', '414', 'N', '2023-01-05 09:30:00.000'),
       ('413', '413230101', '2023-01-05', 5, 5, 'CRO', '413', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, no_def_pos, m_phone, w_phone, h_phone, h_email,
                             police_check, address_line_1, responded, postcode)
values ('415000001', 'Smith0', 'John0', '1980-01-01', 0, '000000001', '000000002', '000000003',
        '415000001@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'AB1 0CD'),
       ('415000002', 'Smith1', 'John1', '1980-01-01', 0, '100000001', '100000002', '100000003',
        '415000002@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'AB1 1CD'),
       ('415000003', 'Smith2', 'John2', '1980-01-01', 0, '200000001', '200000002', '200000003',
        '415000003@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'AB1 2CD'),


       ('414000001', 'Smith3', 'John3', '1980-01-01', 0, '300000001', '300000002', '300000003',
        '414000001@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'AB1 3CD'),
       ('414000002', 'Smith4', 'John4', '1980-01-01', 0, '400000001', '400000002', '400000003',
        '414000002@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'AB1 4CD'),


       ('413000001', 'Smith5', 'John5', '1980-01-01', 0, '500000001', '500000002', '500000003',
        '413000001@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'AB1 5CD'),
       ('413000002', 'Smith6', 'John6', '1980-01-01', 0, '600000001', '600000002', '600000003',
        '413000002@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'AB1 6CD')
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, next_date)
values ('415', '415000001', '415230101', 1, true, '2023-01-01'),
       ('415', '415000002', '415230101', 3, true, '2023-01-02'),
       ('415', '415000003', '415230103', 5, true, '2023-01-04'),

       ('414', '414000001', '414230101', 5, true, '2023-01-05'),
       ('414', '414000002', '414230101', 5, true, '2023-01-06'),

       ('413', '413000001', '413230101', 5, true, '2023-01-07'),
       ('413', '413000002', '413230101', 5, true, '2023-01-08')
;

INSERT INTO juror_mod.trial
(trial_number, loc_code, description, courtroom, judge, trial_type, trial_start_date, trial_end_date, anonymous,
 juror_requested, jurors_sent)
VALUES ('T100000001', '462', 'TEST DEFENDANT62', 999991, 999991, 'CRI', current_date, NULL, false, NULL, NULL),
       ('T100000002', '415', 'TEST DEFENDANT15', 999992, 999992, 'CRI', current_date - 1, NULL, false, NULL, NULL),
       ('T100000003', '415', 'TEST DEFENDANT15', 999992, 999992, 'CRI', current_date - 1, NULL, false, NULL, NULL),
       ('T100000004', '414', 'TEST DEFENDANT16', 999992, 999992, 'CRI', current_date - 1, NULL, false, NULL, NULL);


insert into juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result",
                                   completed)
values ('415', '415000001', 'T100000002', 1, current_date - 1, 'J', false),
       ('415', '415000002', 'T100000002', 1, current_date - 1, 'J', false),
       ('415', '415000003', 'T100000002', 1, current_date - 1, 'J', false),

       ('414', '414000001', 'T100000004', 1, current_date - 1, 'J', false),
       ('414', '414000002', 'T100000004', 1, current_date - 1, 'J', false);

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, attendance_type,
                                  appearance_stage, attendance_audit_number, trial_number, is_draft_expense, hide_on_unpaid_expense_and_reports)
values ('2023-01-01', '415000001', '415230101', '415', 'ABSENT', 'CHECKED_OUT', 'P123', null,false, false),
       ('2023-01-02', '415000001', '415230101', '415', 'FULL_DAY', 'EXPENSE_ENTERED', 'P1234', null,true, false),
       ('2023-01-03', '415000001', '415230101', '415', 'NON_ATTENDANCE', 'EXPENSE_ENTERED', 'J1234', 'T100000002',false, false),
       ('2023-01-04', '415000001', '415230101', '415', 'NON_ATTENDANCE', 'EXPENSE_ENTERED', 'P1234', null,true, false),
       ('2023-01-05', '415000001', '415230101', '415', 'NON_ATTENDANCE', 'EXPENSE_ENTERED', 'J1234', 'T100000002',false, true),
       ('2023-01-06', '415000001', '415230101', '415', 'NON_ATTENDANCE', 'EXPENSE_ENTERED', 'P1234', null,true, true),

       ('2023-01-01', '415000002', '415230101', '415', 'HALF_DAY', 'EXPENSE_AUTHORISED', 'J123', 'T100000002',false, false),
       ('2023-01-02', '415000002', '415230101', '415', 'FULL_DAY', 'EXPENSE_EDITED', 'P1234', null,false, false),
       ('2023-01-03', '415000002', '415230101', '415', 'NON_ATTENDANCE', 'EXPENSE_ENTERED', 'J1231', 'T100000003',false, false),
       ('2023-01-04', '415000002', '415230101', '415', 'NON_ATTENDANCE', 'EXPENSE_ENTERED', 'J1235', 'T100000003',false, false),


       ('2023-01-01', '415000003', '415230103', '415', 'HALF_DAY', 'EXPENSE_AUTHORISED', 'P1234', null,false, false),
       ('2023-01-02', '415000003', '415230103', '415', 'FULL_DAY', 'EXPENSE_EDITED', 'P1234', null,false, false),

       ('2023-01-01', '414000001', '414230101', '414', 'FULL_DAY', 'EXPENSE_ENTERED', 'J1231', 'T100000004',false, false),
       ('2023-01-02', '414000001', '414230101', '414', 'FULL_DAY', 'EXPENSE_ENTERED', 'J1235', 'T100000004',false, false),
       ('2023-01-01', '414000002', '414230101', '414', 'HALF_DAY_LONG_TRIAL', 'EXPENSE_ENTERED', 'J1231', 'T100000004',false, false),
       ('2023-01-02', '414000002', '414230101', '414', 'FULL_DAY', 'EXPENSE_ENTERED', 'J1235', 'T100000004',false, false),

       ('2023-01-01', '413000001', '413230101', '413', 'NON_ATTENDANCE_LONG_TRIAL', 'EXPENSE_ENTERED', 'P1231', null,false, false),
       ('2023-01-02', '413000001', '413230101', '413', 'FULL_DAY', 'EXPENSE_ENTERED', 'P1235', null,false, false),
       ('2023-01-01', '413000002', '413230101', '413', 'FULL_DAY', 'EXPENSE_ENTERED', 'P1231', null,false, false),
       ('2023-01-02', '413000002', '413230101', '413', 'FULL_DAY', 'EXPENSE_ENTERED', 'P1235', null,false, false)
;