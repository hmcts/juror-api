INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
VALUES ('400', '415240101', '2024-01-15', 20, 20, 'CRO', '415', 'N', '2024-01-15 00:09:30.000'),
       ('415', '415240102', '2024-01-15', 20, 20, 'CRO', '415', 'N', '2024-01-15 00:09:30.000'),
       ('415', '767240101', '2024-01-16', 20, 20, 'CRO', '767', 'N', '2024-01-16 00:09:30.000'),

       ('400', '415240103', '2024-01-20', 20, 20, 'CRO', '415', 'N', '2024-01-20 00:09:30.000'),
       ('415', '415240104', '2024-01-20', 20, 20, 'CRO', '415', 'N', '2024-01-20 00:09:30.000');


-- BUREAU DATA: START
INSERT INTO juror_mod.juror (juror_number, first_name, last_name, dob, address_line_1, postcode, responded)
VALUES ('041500001', 'BName1', 'BSurname1', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500002', 'BName2', 'BSurname2', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500003', 'BName3', 'BSurname3', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500004', 'BName4', 'BSurname4', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500005', 'BName5', 'BSurname5', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500006', 'BName6', 'BSurname6', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500007', 'BName7', 'BSurname7', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500008', 'BName8', 'BSurname8', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500009', 'BName9', 'BSurname9', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500010', 'BName10', 'BSurname10', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500011', 'BName11', 'BSurname11', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500012', 'BName12', 'BSurname12', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500013', 'BName13', 'BSurname13', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500014', 'BName14', 'BSurname14', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500015', 'BName15', 'BSurname15', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500016', 'BName16', 'BSurname16', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500017', 'BName17', 'BSurname17', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500018', 'BName18', 'BSurname18', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500019', 'BName19', 'BSurname19', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500020', 'BName20', 'BSurname20', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500021', 'BName21', 'BSurname21', '1980-01-01', 'addressLine1', 'CH1 2AN', true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, next_date)
VALUES ('415', '041500001', '415240101', 2, true, '2024-01-15'),
       ('415', '041500002', '415240101', 2, true, '2024-01-15'),
       ('415', '041500003', '415240101', 2, true, '2024-01-15'),
       ('415', '041500004', '415240101', 2, true, '2024-01-15'),
       ('415', '041500005', '415240101', 2, true, '2024-01-15'),
       ('415', '041500006', '415240101', 6, true, '2024-01-15'),
       ('415', '041500007', '415240101', 9, true, '2024-01-15'),
       ('415', '041500008', '415240101', 9, true, '2024-01-15'),
       ('415', '041500009', '415240101', 7, true, '2024-01-15'),
       ('415', '041500010', '415240101', 7, true, '2024-01-15'),
       ('415', '041500011', '415240101', 7, true, '2024-01-15'),
       ('415', '041500012', '415240101', 8, true, '2024-01-15'),
       ('415', '041500013', '415240101', 10, true, '2024-01-15'),
       ('415', '041500014', '415240101', 10, true, '2024-01-15'),
       ('415', '041500015', '415240101', 10, true, '2024-01-15'),
       ('415', '041500016', '415240101', 10, true, '2024-01-15'),
       ('415', '041500017', '415240101', 12, true, '2024-01-15'),
       ('415', '041500018', '415240101', 5, true, '2024-01-15'),
       ('415', '041500019', '415240103', 2, true, '2024-01-20'),
       ('415', '041500020', '415240103', 2, true, '2024-01-20'),
       ('415', '041500021', '415240103', 2, true, '2024-01-20');
-- BUREAU DATA: END


-- COURT DATA: START
INSERT INTO juror_mod.juror (juror_number, first_name, last_name, dob, address_line_1, postcode, responded)
VALUES ('041501001', 'CName1', 'CSurname1', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501002', 'CName2', 'CSurname2', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501003', 'CName3', 'CSurname3', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501004', 'CName4', 'CSurname4', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501005', 'CName5', 'CSurname5', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501006', 'CName6', 'CSurname6', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501007', 'CName7', 'CSurname7', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501008', 'CName8', 'CSurname8', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501009', 'CName9', 'CSurname9', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501010', 'CName10', 'CSurname10', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501011', 'CName11', 'CSurname11', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501012', 'CName12', 'CSurname12', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501013', 'CName13', 'CSurname13', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501014', 'CName14', 'CSurname14', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501015', 'CName15', 'CSurname15', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501016', 'CName16', 'CSurname16', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501017', 'CName17', 'CSurname17', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501018', 'CName18', 'CSurname18', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501019', 'CName19', 'CSurname19', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501020', 'CName20', 'CSurname20', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041501021', 'CName21', 'CSurname21', '1980-01-01', 'addressLine1', 'CH1 2AN', true),

       ('076701001', 'KName1', 'KSurname1', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('076701002', 'KName2', 'KSurname2', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('076701003', 'KName3', 'KSurname3', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('076701004', 'KName4', 'KSurname4', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('076701005', 'KName5', 'KSurname5', '1980-01-01', 'addressLine1', 'CH1 2AN', true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, next_date)
VALUES ('415', '041501001', '415240102', 2, true, '2024-01-15'),
       ('415', '041501002', '415240102', 2, true, '2024-01-15'),
       ('415', '041501003', '415240102', 2, true, '2024-01-15'),
       ('415', '041501004', '415240102', 2, true, '2024-01-15'),
       ('415', '041501005', '415240102', 2, true, '2024-01-15'),
       ('415', '041501006', '415240102', 6, true, '2024-01-15'),
       ('415', '041501007', '415240102', 9, true, '2024-01-15'),
       ('415', '041501008', '415240102', 9, true, '2024-01-15'),
       ('415', '041501009', '415240102', 7, true, '2024-01-15'),
       ('415', '041501010', '415240102', 7, true, '2024-01-15'),
       ('415', '041501011', '415240102', 7, true, '2024-01-15'),
       ('415', '041501012', '415240102', 8, true, '2024-01-15'),
       ('415', '041501013', '415240102', 10, true, '2024-01-15'),
       ('415', '041501014', '415240102', 10, true, '2024-01-15'),
       ('415', '041501015', '415240102', 10, true, '2024-01-15'),
       ('415', '041501016', '415240102', 10, true, '2024-01-15'),
       ('415', '041501017', '415240102', 12, true, '2024-01-15'),
       ('415', '041501018', '415240102', 5, true, '2024-01-15'),
       ('415', '041501019', '415240104', 2, true, '2024-01-20'),
       ('415', '041501020', '415240104', 2, true, '2024-01-20'),
       ('415', '041501021', '415240104', 2, true, '2024-01-20'),

       ('767', '076701001', '767240101', 2, true, '2024-01-16'),
       ('767', '076701002', '767240101', 2, true, '2024-01-16'),
       ('767', '076701003', '767240101', 3, true, '2024-01-16'),
       ('767', '076701004', '767240101', 4, true, '2024-01-16'),
       ('767', '076701005', '767240101', 1, true, '2024-01-16');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, time_out,
                                  appearance_stage)
VALUES ('2024-01-15', '041501001', '415240102', '415', '09:30:00', NULL, 'CHECKED_IN'),
       ('2024-01-15', '041501002', '415240102', '415', '09:30:00', '17:30:00', 'CHECKED_OUT'),
       ('2024-01-16', '076701003', '767240101', '767', '09:30:00', NULL, 'CHECKED_IN'),
       ('2024-01-16', '076701004', '767240101', '767', '09:30:00', '17:30:00', 'CHECKED_OUT');
-- COURT DATA: END
