
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