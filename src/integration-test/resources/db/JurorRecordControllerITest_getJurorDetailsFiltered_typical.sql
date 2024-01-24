INSERT INTO juror_mod.juror (juror_number, responded,
                             title, first_name, last_name,
                             address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode,
                             sort_code, bank_acct_name, bank_acct_no, bldg_soc_roll_no)
VALUES ('123456789', true,
        'Mr', 'FNAME', 'LNAME',
        'Address Line 1', 'Address Line 2', 'Address Line 3', 'Address Line 4', 'Address Line 5', 'CH1 2AN',
        '112233', 'Bank NAME', '12345678', null),
       ('223456789', true,
        'Miss', 'FNAME2', 'LNAME2',
        'Address Line 1', 'Address Line 2', 'Address Line 3', 'Address Line 4', 'Address Line 5', 'CH1 2AN',
        '332211', 'Bank NAME 2', '87654321', null),
       ('323456789', true,
        'Dr', 'John', 'Joe',
        'Road 1', 'Unknown', 'Person', 'Street', 'Country123', 'BH2 4AN',
        '112233', 'Bank NAME', '12345678', null)
;
INSERT INTO juror_mod.rev_info
    (revision_number, revision_timestamp)
VALUES (1, EXTRACT(EPOCH FROM current_date)),
       (2, EXTRACT(EPOCH FROM current_date)),
       (3, EXTRACT(EPOCH FROM current_date));

INSERT INTO juror_mod.juror_audit
(revision, rev_type, juror_number,
 title, first_name, last_name,
 address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode,
 sort_code, bank_acct_name, bank_acct_no, bldg_soc_roll_no)
VALUES (1, 1, '123456789',
        'Mr1', 'FNAME1', 'LNAME1',
        '1Address Line 1', '1Address Line 2', '1Address Line 3', '1Address Line 4', '1Address Line 5', 'CH1 2AN',
        '111111', '1Bank NAME', '11111111', null),
       (2, 1, '123456789',
        'Mr2', 'FNAME2', 'LNAME2',
        '2Address Line 1', '2Address Line 2', '2Address Line 3', '2Address Line 4', '2Address Line 5', 'CH2 2AN',
        null, null, null, 'Roll Number2'),
       (3, 1, '123456789',
        'Mr3', 'FNAME3', 'LNAME3',
        '3Address Line 1', '3Address Line 2', '3Address Line 3', '3Address Line 4', '3Address Line 5', 'CH3 2AN',
        '333333', '3Bank NAME', '33333333', null);