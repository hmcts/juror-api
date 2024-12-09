
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
('415231103', '415', current_date, 5, '415');

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

insert into juror_mod.juror_pool(owner, juror_number, pool_number, status, is_active, location) values
('415', '415000001', '415231101', 4, true,'415'),
('415', '415000002', '415231101', 4, true,'415'),
('415', '415000003', '415231101', 4, true,'415'),
('415', '415000004', '415231102', 4, true,'415'),
('415', '415000005', '415231102', 4, true,'415'),
('415', '415000006', '415231102', 3, true,'415'),
('415', '415000007', '415231102', 3, true,'415'),
('415', '415000008', '415231102', 3, true,'415'),
('415', '415000009', '415231103', 3, true,'415'),
('415', '415000010', '415231103', 3, true,'415');

insert into juror_mod.appearance (attendance_date, juror_number,loc_code, time_in, time_out, attendance_type, appearance_stage) values
(current_date - 1, '415000001', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000002', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000003', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000004', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000005', '415','09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000006', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000007', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000008', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000009', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' ),
(current_date - 1, '415000010', '415', '09:00','16:00','FULL_DAY','EXPENSE_ENTERED' );

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

insert into juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result", completed) values
('415', '415000001','T10000000', 10, current_date - 1, 'J', false),
('415', '415000002','T10000000', 5, current_date - 1, 'J', false),
('415', '415000003','T10000000', 11, current_date - 1, 'J', false),
('415', '415000004','T10000000', 1, current_date - 1, 'J', false),
('415', '415000005','T10000000', 3, current_date - 1, 'J', false),
('415', '415000006','T10000000', 9, current_date - 1, 'R', true),
('415', '415000006','T10000001', 4, current_date - 1, null, false),
('415', '415000007','T10000001', 7, current_date - 1, null, false),
('415', '415000008','T10000001', 8, current_date - 1, null, false),
('415', '415000009','T10000001', 13, current_date - 1, null, false),
('415', '415000010','T10000001', 12, current_date - 1, null, false);
