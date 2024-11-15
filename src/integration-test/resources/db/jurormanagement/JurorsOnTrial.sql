
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
('415000017','LNAME','FNAME','ADDRESS LINE 1', true),
('415000018','LNAME','FNAME','ADDRESS LINE 1', true),
('415000019','LNAME','FNAME','ADDRESS LINE 1', true),
('415000020','LNAME','FNAME','ADDRESS LINE 1', true);

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
('415', '415000017', '415231104', 4, true,'415'),
('415', '415000018', '415231104', 4, true,'415'),
('415', '415000019', '415231104', 4, true,'415'),
('415', '415000020', '415231104', 4, true,'415');

insert into juror_mod.judge (owner, code, description) values
('415', '0001', 'judge dredd'),
('415', '0002', 'judge jose'),
('415', '0003', 'judge June'),
('415', '0004', 'judge Jack');

insert into juror_mod.courtroom (loc_code, room_number, description) values
('415', '1', 'big room'),
('415', '2', 'small room'),
('415', '3', 'other room'),
('415', '4', 'outer room');

insert into juror_mod.trial (trial_number,loc_code,description,courtroom,judge,trial_type,trial_start_date,anonymous) values
('T10000000', '415', 'test trial', 1, 1, 'CIV', current_date, false),
('T10000001', '415', 'test trial', 2, 2, 'CIV', current_date, false),
('T10000002', '415', 'test trial', 3, 3, 'CIV', current_date, false),
('T10000003', '415', 'test trial', 4, 4, 'CIV', current_date, false);

insert into juror_mod.appearance (attendance_date, juror_number, loc_code, time_in, time_out, non_attendance,
attendance_type, trial_number, appearance_stage, attendance_audit_number) values
(current_date, '415000001', '415', current_time,null,false,null,null,'CHECKED_IN',null),
(current_date, '415000002', '415', current_time,null,false,null,null,'CHECKED_IN',null),
(current_date, '415000003', '415', current_time,null,false,null,null,'CHECKED_IN',null),
(current_date, '415000004', '415', current_time,null,false,null,null,'CHECKED_IN',null),
(current_date, '415000005', '415', current_time,null,false,null,null,'CHECKED_IN',null),
(current_date, '415000006', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000007', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000008', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000009', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000010', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000011', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000012', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000013', '415', current_time,current_time,false,'FULL_DAY',null,'EXPENSE_ENTERED','P00000001'),
(current_date, '415000014', '415', current_time,current_time,true,'NON_ATTENDANCE','T10000001','EXPENSE_ENTERED',null),
(current_date, '415000015', '415', current_time,current_time,false,'HALF_DAY','T10000001','EXPENSE_ENTERED','J00000002'),
(current_date, '415000016', '415', null,null,false,'ABSENT','T10000001','EXPENSE_ENTERED',null),
(current_date, '415000017', '415', current_time,current_time,false,'FULL_DAY','T10000001','EXPENSE_ENTERED','J00000002'),
(current_date, '415000018', '415', current_time,current_time,false,'HALF_DAY_LONG_TRIAL','T10000002','EXPENSE_ENTERED','J00000003'),
(current_date, '415000019', '415', current_time,current_time,false,'FULL_DAY_LONG_TRIAL','T10000002','EXPENSE_ENTERED','J00000003'),
(current_date, '415000020', '415', current_time,current_time,true,'NON_ATTENDANCE_LONG_TRIAL','T10000002','EXPENSE_ENTERED',null);


insert into juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result", completed, return_date) values
('415', '415000001','T10000000', 10, current_date - 30, 'R', false, current_date - 30),
('415', '415000002','T10000000', 5, current_date - 30, 'R', false, current_date - 30),
('415', '415000003','T10000000', 11, current_date - 30, 'R', false, current_date - 30),
('415', '415000004','T10000000', 1, current_date - 30, 'R', false, current_date - 30),
('415', '415000005','T10000000', 3, current_date - 30, 'R', false, current_date - 30),
('415', '415000006','T10000000', 4, current_date - 30, 'R', false, current_date - 30),
('415', '415000007','T10000000', 7, current_date - 30, 'R', false, current_date - 30),
('415', '415000008','T10000003', 8, current_date - 30, 'P', false, null),
('415', '415000009','T10000003', 13, current_date - 30, 'P', false, null),
('415', '415000010','T10000003', 12, current_date - 30, 'P', false, null),
('415', '415000011','T10000003', 6, current_date - 30, 'P', false, null),
('415', '415000012','T10000003', 2, current_date - 30, 'P', false, null),
('415', '415000013','T10000003', 9, current_date - 30, 'P', false, null),
('415', '415000014','T10000001', 14, current_date - 30, 'J', false, null),
('415', '415000015','T10000001', 15, current_date - 30, 'J', false, null),
('415', '415000016','T10000001', 16, current_date - 30, 'J', false, null),
('415', '415000017','T10000001', 17, current_date - 30, 'J', false, null),
('415', '415000018','T10000002', 18, current_date - 30, 'J', false, null),
('415', '415000019','T10000002', 20, current_date - 30, 'J', false, null),
('415', '415000020','T10000002', 19, current_date - 30, 'J', false, null);
