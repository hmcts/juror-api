INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
VALUES ('415', '415240101', current_date, 20, 20, 'CRO', '415', 'N', current_date),
       ('415', '415240102', current_date, 20, 20, 'CIV', '415', 'N', current_date),
       ('415', '415240103', current_date, 20, 20, 'CRO', '415', 'N', current_date);

INSERT INTO juror_mod.juror (juror_number, first_name, last_name, dob, address_line_1, postcode, responded,
                             completion_date)
VALUES ('041500001', 'CName1', 'CSurname1', '1980-01-01', 'addressLine1', 'CH1 2AN', true, '2024-01-15'),
       ('041500002', 'CName2', 'CSurname2', '1980-01-01', 'addressLine1', 'CH1 2AN', true, null);


INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, next_date)
VALUES ('415', '041500001', '415240101', 2, true, current_date),
       ('415', '041500002', '415240101', 2, true, current_date);


INSERT INTO juror_mod.pending_juror (juror_number, first_name, last_name, dob, address_line_1, address_line_2,
                                     address_line_3, address_line_4, postcode, date_added, date_created, pool_number,
                                     status, added_by, responded)
VALUES ('041500001', 'CName1', 'CSurname1', '1980-01-01',
        '1 addressLine1', '1 addressLine2', '1 addressLine3', '1 addressLine4', 'CH1 1AN',
        '2024-01-01', '2024-01-01', '415240101', 'A', 'test_court_standard', true),
       ('041500002', 'CName2', 'CSurname2', '1980-01-02',
        '2 addressLine1', '2 addressLine2', '2 addressLine3', '2 addressLine4', 'CH1 2AN',
        '2024-01-02', '2024-01-01', '415240101', 'A', 'test_court_standard', true),
       ('041500003', 'CName3', 'CSurname3', '1980-01-03',
        '3 addressLine1', '3 addressLine2', '3 addressLine3', '3 addressLine4', 'CH1 3AN',
        '2024-01-20', '2024-01-01', '415240102', 'Q', 'test_court_standard', true),
       ('041500004', 'CName4', 'CSurname4', '1980-01-04',
        '4 addressLine1', '4 addressLine2', ' 4addressLine3', '4 addressLine4', 'CH1 4AN',
        '2024-01-21', '2024-01-01', '415240103', 'Q', 'test_court_standard', true),
       ('041500005', 'CName5', 'CSurname5', '1980-01-05',
        '5 addressLine1', '5 addressLine2', ' 5addressLine3', '5 addressLine4', 'CH1 5AN',
        '2024-01-01', '2024-01-01', '415240103', 'R', 'test_court_standard', true),
       ('041500006', 'CName6', 'CSurname6', '1980-01-06',
        '6 addressLine1', '6 addressLine2', ' 6 addressLine3', '6 addressLine4', 'CH1 5AN',
        '2024-02-21', '2024-02-21', '415240103', 'A', 'test_court_standard', true);
