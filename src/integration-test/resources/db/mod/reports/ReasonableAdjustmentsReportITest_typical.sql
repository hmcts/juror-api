-- create a pool for court location 415
INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
VALUES ('400', '415240101', '2024-01-01', 5, 5, 'CRO', '415', 'N', '2024-01-01 09:30:00.000'),
       ('400', '408240101', '2024-01-02', 5, 5, 'CRO', '408', 'N', '2024-01-02 09:30:00.000');

-- create juror records
INSERT INTO juror_mod.juror (juror_number, first_name, last_name, dob, h_phone, m_phone, w_phone, h_email,
                             address_line_1, responded, postcode, reasonable_adj_code, reasonable_adj_msg)
VALUES ('041500001', 'CName1', 'CSurname1', '1980-01-01', '000000001', '000000002', '000000003',
        '041500001@email.gov.uk', 'addressLine1', true, 'CH1 2AN', 'D', 'has got allergies'),
       ('041500002', 'CName2', 'CSurname2', '1980-01-01', '100000001', '100000002', '100000003',
        '041500002@email.gov.uk', 'addressLine1', true, 'CH1 2AN', 'M', 'multiple requests'),
       ('041500003', 'CName3', 'CSurname3', '1980-01-01', '200000001', '200000002', '200000003',
        '041500003@email.gov.uk', 'addressLine1', true, 'CH1 2AN', 'M', 'multiple requests'),
       ('041500004', 'CName4', 'CSurname4', '1980-01-01', '300000001', '300000002', '300000003',
        '041500004@email.gov.uk', 'addressLine1', true, 'CH1 2AN', 'T', 'no transport available'),
       ('041500005', 'CName5', 'CSurname5', '1980-01-01', '400000001', '400000002', '400000003',
        '041500005@email.gov.uk', 'addressLine1', true, 'CH1 2AN', 'O', 'other reasons'),
       ('041500006', 'CName6', 'CSurname6', '1980-01-01', '700000001', '700000002', '700000003',
        '041500006@email.gov.uk', 'addressLine1', true, 'CH1 2AN', NULL, NULL),
       ('040800001', 'BName1', 'BSurname1', '1980-01-01', '500000001', '500000002', '500000003',
        '040800001@email.gov.uk', 'addressLine1', true, 'BR1 2AN', 'U', 'needs medication'),
       ('040800002', 'BName2', 'BSurname2', '1980-01-01', '600000001', '600000002', '600000003',
        '040800002@email.gov.uk', 'addressLine1', true, 'BR1 2AN', 'M', 'multiple requests'),
       ('040800003', 'BName3', 'BSurname3', '1980-01-01', '800000001', '800000002', '800000003',
        '040800002@email.gov.uk', 'addressLine1', true, 'BR1 2AN', NULL, NULL);

-- create juror_pool associative records
INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, next_date)
VALUES ('400', '041500001', '415240101', 2, true, '2024-01-01'),
       ('400', '041500002', '415240101', 2, true, '2024-01-01'),
       ('400', '041500003', '415240101', 2, true, '2024-01-01'),
       ('400', '041500004', '415240101', 2, true, '2024-01-01'),
       ('415', '041500005', '415240101', 2, true, '2024-01-01'),
       ('415', '041500006', '415240101', 2, true, '2024-01-01'),
       ('400', '040800001', '408240101', 2, true, '2024-01-02'),
       ('400', '040800002', '408240101', 2, true, '2024-01-02'),
       ('400', '040800003', '408240101', 2, true, '2024-01-02');

-- add reasonable adjustments to its table
INSERT INTO juror_mod.juror_reasonable_adjustment (juror_number, reasonable_adjustment, reasonable_adjustment_detail)
VALUES ('041500001', 'M', 'multiple requests'),
       ('041500002', 'M', 'multiple requests'),
       ('041500003', 'M', 'multiple requests'),
       ('041500004', 'M', 'multiple requests'),
       ('041500005', 'M', 'multiple requests'),
       ('040800001', 'M', 'multiple requests'),
       ('040800002', 'M', 'multiple requests');
