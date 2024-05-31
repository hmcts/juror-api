-- create a pool for court location 415
INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request, attend_time)
VALUES ('400', '400000001', '2023-01-05', 3, 3, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '400000002', '2023-01-05', 3, 3, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');
-- Jurors
INSERT INTO juror_mod.juror (juror_number, first_name, last_name, postcode, address_line_1, responded)
VALUES ('200000001', 'John1', 'Smith1', 'AD1 2HP', 'House', true),
       ('200000002', 'John2', 'Smith2', 'AD2 2HP', 'House', true),
       ('200000003', 'John3', 'Smith3', 'AD3 2HP', 'House', true),
       ('200000004', 'Jane4', 'Smith4', 'AD4 2HP', 'House', true),
       ('200000005', 'Jane5', 'Smith5', 'AD5 2HP', 'House', true),
       ('200000006', 'Jane6', 'Smith6', 'AD6 2HP', 'House', true);

INSERT INTO juror_mod.judge (id, owner, code, description)
VALUES (1, '415', '0001', 'judge dredd'),
       (2, '415', '0002', 'judge jose');

INSERT INTO juror_mod.courtroom (id, loc_code, room_number, description)
VALUES (1, '415', '1', 'big room'),
       (2, '415', '2', 'small room');

INSERT INTO juror_mod.trial (trial_number,loc_code,description,courtroom,judge,trial_type,trial_start_date,trial_end_date,anonymous,juror_requested,jurors_sent)
VALUES ('T000000001','415','test trial',1,1,'CIV','2024-05-08',NULL,false,NULL,NULL),
       ('T000000002','415','test trial',2,2,'CIV','2024-04-25',NULL,true,NULL,NULL);

INSERT INTO juror_mod.juror_trial (loc_code,juror_number,trial_number,rand_number,date_selected,"result",completed,empanelled_date,return_date)
VALUES ('415','200000001','T000000001',10,'2024-05-08 00:00:00','J',true,'2024-05-08',NULL),
       ('415','200000002','T000000001',10,'2024-05-08 00:00:00','J',true,'2024-05-08',NULL),
       ('415','200000003','T000000001',10,'2024-05-08 00:00:00','J',true,'2024-05-08',NULL),
       ('415','200000004','T000000002',10,'2024-05-08 00:00:00','J',true,'2024-05-08',NULL),
       ('415','200000005','T000000002',10,'2024-05-08 00:00:00','J',true,'2024-05-08',NULL),
       ('415','200000006','T000000002',10,'2024-05-08 00:00:00','J',true,'2024-05-08',NULL);
