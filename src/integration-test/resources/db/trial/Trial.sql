-- Reset sequence numbers
SELECT setval('juror_mod.courtroom_id_seq', 65);
SELECT setval('juror_mod.judge_id_seq', 20);

-- Dummy test data
insert into juror_mod.judge (id, owner, code, description) values
(21,'415', '1234','Test judge'),
(22,'415', '4321','Judge Test'),
(23,'416', '4322','Judge Test2'),
(24,'416', '4323','Judge Test3');

insert into juror_mod.courtroom (id, loc_code, room_number, description) values
(66,'415', '1', 'large room fits 100 people'),
(67,'462', '2', 'large room fits 100 people'),
(68,'416', '3', 'large room fits 101 people'),
(69,'416', '4', 'large room fits 102 people');

insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, trial_end_date, anonymous, courtroom) values
('T100000000','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000001','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000002','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000003','415', 'TEST DEFENDANT', 21,'CIV', current_date, current_date, false, 66),
('T100000004','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000005','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000006','462', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 67),
('T100000007','415', 'TEST DEFENDANT', 21,'CIV', current_date, current_date, false, 66),
('T100000008','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000009','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000010','462', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 67),
('T100000011','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000012','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000013','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000014','462', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 67),
('T100000015','415', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 66),
('T100000016','462', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 67),
('T100000017','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000018','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000019','415', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 66),
('T100000020','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000021','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000022','462', 'TEST DEFENDANT', 21,'CIV', current_date, current_date, false, 67),
('T100000023','415', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 66),
('T100000024','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000025','415', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 66),
('T100000026','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date,false, 67),
('TEST000012','416', 'JOE BLOGGS', 23,'CIV', current_date, null,false, 68);

INSERT INTO juror_mod.trial
(trial_number, loc_code, description, courtroom, judge, trial_type, trial_start_date, trial_end_date, anonymous, juror_requested, jurors_sent) VALUES
('T100000027', '462', 'TEST DEFENDANT62', 67, 22, 'CRI', current_date, NULL, false, NULL, NULL),
('T100000027', '415', 'TEST DEFENDANT15', 66, 21, 'CRI', current_date - 1, NULL, false, NULL, NULL);

