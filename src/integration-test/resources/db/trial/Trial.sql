-- Reset sequence numbers
SELECT setval('juror_mod.courtroom_id_seq', 65);
SELECT setval('juror_mod.judge_id_seq', 20);

-- Dummy test data
insert into juror_mod.judge (owner, code, description) values
('415', '1234','Test judge'),
('462', '4321','Judge Test');

insert into juror_mod.courtroom (loc_code, room_number, description) values
('415', '1', 'large room fits 100 people'),
('462', '2', 'large room fits 100 people');

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
('T100000026','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date,false, 67);