
ALTER SEQUENCE juror_mod.judge_id_seq
RESTART WITH 1;
ALTER SEQUENCE juror_mod.courtroom_id_seq
RESTART WITH 1;

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
('415231104', '415', current_date, 5, '415'),
('415231105', '415', current_date, 5, '415');


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
('415000020','LNAME','FNAME','ADDRESS LINE 1', true),
('415000021','LNAME','FNAME','ADDRESS LINE 1', true),
('415000022','LNAME','FNAME','ADDRESS LINE 1', true),
('415000023','LNAME','FNAME','ADDRESS LINE 1', true),
('415000024','LNAME','FNAME','ADDRESS LINE 1', true),
('415000025','LNAME','FNAME','ADDRESS LINE 1', true),
('415000026','LNAME','FNAME','ADDRESS LINE 1', true),
('415000027','LNAME','FNAME','ADDRESS LINE 1', true),
('415000028','LNAME','FNAME','ADDRESS LINE 1', true),
('415000029','LNAME','FNAME','ADDRESS LINE 1', true),
('415000030','LNAME','FNAME','ADDRESS LINE 1', true),
('415000031','LNAME','FNAME','ADDRESS LINE 1', true),
('415000032','LNAME','FNAME','ADDRESS LINE 1', true);

insert into juror_mod.juror_pool(owner, juror_number, pool_number, status, is_active, location, times_sel)
values
('415', '415000001', '415231101', 2, true,'415',0),
('415', '415000002', '415231101', 2, true,'415',0),
('415', '415000003', '415231101', 2, true,'415',0),
('415', '415000004', '415231102', 2, true,'415',1),
('415', '415000005', '415231102', 2, true,'415',1),
('415', '415000006', '415231102', 2, true,'415',0),
('415', '415000007', '415231102', 2, true,'415',0),
('415', '415000008', '415231102', 2, true,'415',0),
('415', '415000009', '415231103', 2, true,'415',1),
('415', '415000010', '415231103', 2, true,'415',1),
('415', '415000011', '415231103', 2, true,'415',0),
('415', '415000012', '415231103', 2, true,'415',0),
('415', '415000013', '415231103', 2, true,'415',1),
('415', '415000014', '415231104', 2, true,'415',1),
('415', '415000015', '415231104', 2, true,'415',0),
('415', '415000016', '415231104', 2, true,'415',0),
('415', '415000017', '415231104', 2, true,'415',0),
('415', '415000018', '415231104', 2, true,'415',0),
('415', '415000019', '415231104', 2, true,'415',0),
('415', '415000020', '415231104', 2, true,'415',0),
('415', '415000021', '415231104', 2, true,'415',0),
('415', '415000022', '415231104', 2, true,'415',0),
('415', '415000023', '415231104', 2, true,'415',0),
('415', '415000024', '415231104', 2, true,'415',0),
('415', '415000025', '415231104', 2, true,'415',0),
('415', '415000026', '415231104', 2, true,'415',0),
('415', '415000027', '415231104', 2, true,'415',0),
('415', '415000028', '415231105', 3, true,'415',0),
('415', '415000029', '415231105', 3, true,'415',0),
('415', '415000030', '415231105', 3, true,'415',0),
('415', '415000031', '415231105', 3, true,'415',0),
('415', '415000032', '415231105', 3, true,'415',0);

insert into juror_mod.appearance (attendance_date,juror_number,loc_code, time_in, misc_total_paid, appearance_stage, non_attendance) values
(current_date, '415000001', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000002', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000003', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000004', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000005', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000006', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000007', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000008', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000009', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000010', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000011', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000012', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000013', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000014', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000015', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000016', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000017', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000018', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000019', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000020', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000021', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000022', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000023', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000024', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000025', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000026', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000027', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000028', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000029', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000030', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000031', '415', current_time,0,'CHECKED_IN',false),
(current_date, '415000032', '415', current_time,0,'CHECKED_IN',false);

insert into juror_mod.judge (owner, code, description) values
('415', '0001', 'judge dredd');

insert into juror_mod.courtroom (loc_code, room_number, description) values
('415', '1', 'big room');

insert into juror_mod.trial (trial_number,loc_code,description,courtroom,judge,trial_type,trial_start_date,anonymous) values
('T10000000', '415', 'test trial', 1, 1, 'CIV', current_date, false),
('T10000001', '415', 'test trial', 1, 1, 'CIV', current_date, false),
('T10000002', '415', 'test trial', 1, 1, 'CIV', current_date, false);

