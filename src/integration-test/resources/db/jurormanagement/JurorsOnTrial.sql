
ALTER SEQUENCE juror_mod.judge_id_seq
RESTART WITH 1;
ALTER SEQUENCE juror_mod.courtroom_id_seq
RESTART WITH 1;

delete from juror_mod.juror_response;
delete from juror_mod.juror_history;
delete from juror_mod.appearance ;
delete from juror_mod.juror_pool;
delete from juror_mod.pool;
delete from juror_mod.juror;
delete from juror_mod.trial;
delete from juror_mod.judge;
delete from juror_mod.courtroom ;
delete from juror_mod.juror_trial;

insert into juror_mod.pool (pool_no,owner,return_date,total_no_required, loc_code) values
('415231101', '415', current_date, 5, '415'),
('415231102', '415', current_date, 5, '415'),
('415231103', '415', current_date, 5, '415'),
('415231104', '415', current_date, 5, '415');

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
('415000010','LNAME','FNAME','ADDRESS LINE 1', true),
('415000011','LNAME','FNAME','ADDRESS LINE 1', true),
('415000012','LNAME','FNAME','ADDRESS LINE 1', true),
('415000013','LNAME','FNAME','ADDRESS LINE 1', true),
('415000014','LNAME','FNAME','ADDRESS LINE 1', true),
('415000015','LNAME','FNAME','ADDRESS LINE 1', true),
('415000016','LNAME','FNAME','ADDRESS LINE 1', true),
('415000017','LNAME','FNAME','ADDRESS LINE 1', true);

insert into juror_mod.juror_pool(owner, juror_number, pool_number, status, is_active, location) values
('415', '415000001', '415231101', 3, true,'415'),
('415', '415000002', '415231101', 3, true,'415'),
('415', '415000003', '415231101', 3, true,'415'),
('415', '415000004', '415231102', 3, true,'415'),
('415', '415000005', '415231102', 3, true,'415'),
('415', '415000006', '415231102', 3, true,'415'),
('415', '415000007', '415231102', 3, true,'415'),
('415', '415000008', '415231102', 3, true,'415'),
('415', '415000009', '415231103', 3, true,'415'),
('415', '415000010', '415231103', 3, true,'415'),
('415', '415000011', '415231103', 3, true,'415'),
('415', '415000012', '415231103', 3, true,'415'),
('415', '415000013', '415231103', 3, true,'415'),
('415', '415000014', '415231104', 4, true,'415'),
('415', '415000015', '415231104', 4, true,'415'),
('415', '415000016', '415231104', 4, true,'415'),
('415', '415000017', '415231104', 4, true,'415');

insert into juror_mod.appearance (attendance_date, juror_number,loc_code, time_in, time_out, non_attendance, attendance_type) values
(current_date, '415000001', '415', current_time,null,false,null),
(current_date, '415000002', '415', current_time,null,false,null),
(current_date, '415000003', '415', current_time,null,false,null),
(current_date, '415000004', '415', current_time,null,false,null),
(current_date, '415000005', '415', current_time,null,false,null),
(current_date, '415000006', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000007', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000008', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000009', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000010', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000011', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000012', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000013', '415', current_time,current_time,false,'FULL_DAY'),
(current_date, '415000014', '415', null,null,false,null),
(current_date, '415000015', '415', null,null,false,null),
(current_date, '415000016', '415', null,null,false,null),
(current_date, '415000017', '415', null,null,false,null);

insert into juror_mod.judge (owner, code, description) values
('415', '0001', 'judge dredd'),
('415', '0002', 'judge jose');

insert into juror_mod.courtroom (loc_code, room_number, description) values
('415', '1', 'big room'),
('415', '2', 'small room');

insert into juror_mod.trial (trial_number,loc_code,description,courtroom,judge,trial_type,trial_start_date,anonymous) values
('T10000000', '415', 'test trial', 1, 1, 'CIV', current_date, false),
('T10000001', '415', 'test trial', 2, 2, 'CIV', current_date, false);

INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000001','T10000000', '415231101', 10, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000002','T10000000', '415231101', 5, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000003','T10000000', '415231101', 11, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000004','T10000000', '415231102', 1, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000005','T10000000', '415231102', 3, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000006','T10000000', '415231102', 4, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000007','T10000000', '415231102', 7, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000008','T10000000', '415231102', 8, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000009','T10000000', '415231103', 13, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000010','T10000000', '415231103', 12, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000011','T10000000', '415231103', 6, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000012','T10000000', '415231103', 2, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000013','T10000000', '415231103', 9, current_date - 30, 'P', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000014','T10000001', '415231104', 9, current_date - 30, 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000015','T10000001', '415231104', 9, current_date - 30, 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000016','T10000001', '415231104', 9, current_date - 30, 'J', false);
INSERT INTO juror_mod.juror_trial
(loc_code, juror_number, trial_number, pool_number, rand_number, date_selected, "result", completed)
VALUES('415', '415000017','T10000001', '415231104', 9, current_date - 30, 'J', false);