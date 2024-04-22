SELECT setval('juror_mod.courtroom_id_seq', 1);
SELECT setval('juror_mod.judge_id_seq', 1);

INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
	 ('415220502','415','2023-03-08',0,NULL,'415','Y',NULL,NULL,NULL,false,100,'2024-02-14 00:00:00'),
	 ('415220503','457','2023-03-08',0,NULL,'457','Y',NULL,NULL,NULL,false,100,'2024-02-14 00:00:00');

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, responded, welsh, bureau_transfer_date, date_excused, excusal_code, acc_exc) values
	('123456789','LNAME','FNAME','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true, null,'2023-12-13','2024-02-14 00:00:00.000','B', 'Y'),
	('987654321','LNAME','FNAME','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true, true,'2023-12-13','2024-02-14 00:00:00.000','C', 'Y');

insert into juror_mod.juror_pool (juror_number , pool_number, "owner", status, date_created, is_active, "location") values
	('123456789','415220502', '415', 3, '2024-01-17 00:00:00.000',true,'415'),
	('987654321','415220503', '457', 3, '2024-01-17 00:00:00.000',true,'457');


-- Dummy test data
insert into juror_mod.judge (owner, code, description) values
	('415', '1234','Test judge'),
	('457', '4321','Judge Test');

insert into juror_mod.courtroom (loc_code, room_number, description) values
	('415', '1', 'large room fits 100 people'),
	('457', '2', 'large room fits 100 people');

insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, anonymous, courtroom) values
	('T10000000','415', 'TEST DEFENDANT', 2,'CIV', current_date, false, 2),
	('T10000002','415', 'TEST DEFENDANT', 2,'CIV', current_date, false, 2),
	('T10000001','457', 'TEST DEFENDANT', 3,'CIV', current_date, false, 3);


INSERT INTO juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result", completed) values
	('415', '123456789','T10000000', 10, '2024-02-22 13:50:59.110', '', false),
	('457', '987654321','T10000001', 5, '2024-02-22 13:50:58.821', '', false);
