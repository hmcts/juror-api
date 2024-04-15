
insert into juror_mod.juror (juror_number,last_name,first_name,address_line_1,responded) values
('415000001','LNAME','FNAME','ADDRESS LINE 1', true),
('415000002','LNAME','FNAME','ADDRESS LINE 1', true),
('415000003','LNAME','FNAME','ADDRESS LINE 1', true),
('415000004','LNAME','FNAME','ADDRESS LINE 1', true),
('415000005','LNAME','FNAME','ADDRESS LINE 1', true),
('415000006','LNAME','FNAME','ADDRESS LINE 1', true),
('415000007','LNAME','FNAME','ADDRESS LINE 1', true),
('415000008','LNAME','FNAME','ADDRESS LINE 1', true),
('415000009','LNAME','FNAME','ADDRESS LINE 1', true),
('415000010','LNAME','FNAME','ADDRESS LINE 1', true);

insert into juror_mod.appearance (attendance_date, juror_number,loc_code, time_in, time_out, non_attendance) values
(current_date, '415000001', '415', current_time,null,false),
(current_date, '415000002', '415', current_time,null,false),
(current_date, '415000003', '415', current_time,null,false),
(current_date, '415000004', '415', current_time,null,false),
(current_date, '415000005', '415', current_time,null,false),
(current_date, '415000006', '415', current_time,current_time,false),
(current_date, '415000007', '415', current_time,current_time,false),
(current_date, '415000008', '415', current_time,current_time,false),
(current_date, '415000009', '415', current_time,current_time,false),
(current_date, '415000010', '415', current_time,current_time,false);

insert into juror_mod.judge (owner, code, description) values
('415', '0001', 'judge dredd'),
('415', '0002', 'judge jose');

insert into juror_mod.courtroom (loc_code, room_number, description) values
('415', '1', 'big room'),
('415', '2', 'small room');

insert into juror_mod.trial (trial_number,loc_code,description,courtroom,judge,trial_type,trial_start_date,anonymous) values
('T10000000', '415', 'test trial', 1, 1, 'CIV', current_date, false),
('T10000001', '415', 'test trial', 2, 2, 'CIV', current_date, false),
('T10000002', '415', 'test trial', 2, 2, 'CIV', current_date, false);

INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000001','T10000000', '415231101', 10, '2023-11-30 13:50:59.110', 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000002','T10000000', '415231101', 5, '2023-11-30 13:50:58.821', 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000003','T10000000', '415231101', 11, '2023-11-30 13:50:59.162', 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000004','T10000000', '415231102', 1, '2023-11-30 13:50:58.492', 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000005','T10000000', '415231102', 3, '2023-11-30 13:50:58.678', 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000006','T10000001', '415231102', 4, '2023-11-30 13:50:58.750', 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000007','T10000001', '415231102', 7, '2023-11-30 13:50:58.952', 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000008','T10000001', '415231102', 8, '2023-11-30 13:50:59.007', 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000009','T10000001', '415231103', 13, '2023-11-30 13:50:59.226', 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000010','T10000001', '415231103', 12, '2023-11-30 13:50:59.197', 'P', false);
