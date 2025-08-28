INSERT INTO juror_mod.juror (juror_number, first_name, last_name, h_email, title, dob, address_line_1, address_line_2, address_line_3,address_line_4, postcode, h_phone, w_phone, m_phone, responded, poll_number, contact_preference)
VALUES
('586856851', 'Cynth', 'BOWEN', 'cbrownere1@yellowpagres.com', 'Rev', TO_DATE('1987-05-08 13:42:18', 'YYYY-MM-DD HH24:MI:SS'), '7 Lunder Park', 'England', 'Weston', 'United Kingdom', 'CF62SW', '44(211)698-2662', '44(551)736-7419','44(520)949-8284', 'N', 19917,  0),
('487498307', 'Kim', 'PIERE', 'kpierceer2@plalar.or.jp', 'Mr', TO_DATE('1975-02-11 19:57:43', 'YYYY-MM-DD HH24:MI:SS'), '9371 Superior Avenue', 'England', 'Newbiggin', 'United Kingdom', 'NE46TG', '44(513)905-6359', '44(965)467-9567', '44(373)639-7785','N', 93315,  0),
('472008411', 'Den', 'ROBERTSONLONGNAME', 'drobertsoerrn3@hibur.com', 'Honorable', TO_DATE('1962-03-07 13:50:05', 'YYYY-MM-DD HH24:MI:SS'), '57 Westerfield Parkway', 'England', 'Charlton', 'United Kingdom', 'OX12RV', '44(202)757-0949', '44(665)723-2987', '44(170)995-1393','N', 93382,  0),
('209092530', 'Janet', 'CASTILLONI', 'jcastillort0@edr.gov', 'Dr', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB39RY', '44(703)209-6993', '44(109)549-5625', '44(145)525-2390', 'N', 21112, 0);


INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
('555555555','415','2022-05-03',5,'CRO','415','N',NULL,NULL,NULL,false,5,NULL);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES
('415', '586856851', '555555555', true, '2022-05-03', 1),
('415', '472008411', '555555555', true, '2022-05-03', 1),
('415', '487498307', '555555555', true, '2022-05-03', 1),
('415', '209092530', '555555555', true, '2022-05-03', 1);


INSERT INTO juror_mod.JUROR_RESPONSE (JUROR_NUMBER,DATE_RECEIVED,TITLE,FIRST_NAME,LAST_NAME,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,PROCESSING_STATUS,DATE_OF_BIRTH,PHONE_NUMBER,ALT_PHONE_NUMBER,EMAIL,THIRDPARTY_REASON,RESIDENCY,RESIDENCY_DETAIL,MENTAL_HEALTH_ACT,MENTAL_HEALTH_ACT_DETAILS,BAIL,BAIL_DETAILS,CONVICTIONS,CONVICTIONS_DETAILS,DEFERRAL_REASON,DEFERRAL_DATE,reasonable_adjustments_arrangements,EXCUSAL_REASON, VERSION, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE)
VALUES ('209092530', CURRENT_DATE,null,null,null,'address',null,null,null,null,'RG16HA','TODO',null,null,null,null,null,'0',null,'0',null,'0',null,'0',null,null,null,null,null,555,'Y', 'Y', 'ncrawford' , CURRENT_DATE);

INSERT INTO juror_mod.JUROR_RESPONSE (JUROR_NUMBER,DATE_RECEIVED,TITLE,FIRST_NAME,LAST_NAME,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,PROCESSING_STATUS,DATE_OF_BIRTH,PHONE_NUMBER,ALT_PHONE_NUMBER,EMAIL,THIRDPARTY_REASON,RESIDENCY,RESIDENCY_DETAIL,MENTAL_HEALTH_ACT,MENTAL_HEALTH_ACT_DETAILS,BAIL,BAIL_DETAILS,CONVICTIONS,CONVICTIONS_DETAILS,DEFERRAL_REASON,DEFERRAL_DATE,reasonable_adjustments_arrangements,EXCUSAL_REASON, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE)
VALUES
('586856851',(SELECT CURRENT_DATE-3),null,null,null,'address',null,null,null,null,'CF86HA',null,null,null,null,null,null,'0',null,'0',null,'0',null,'0',null,null,null,'nuts allergy',null,'Y', 'Y', 'ncrawford' , CURRENT_DATE),
('487498307',(SELECT CURRENT_DATE-9),null,null,null,'address',null,null,null,null,null,'TODO',null,null,null,null,null,'0',null,'0',null,'0',null,'0',null,null,null,'wheelchair access',null,'Y', 'Y', 'ncrawford' , CURRENT_DATE),
('472008411',(SELECT CURRENT_DATE-10),null,null,null,'address',null,null,null,null,null,'AWAITING_CONTACT',null,null,null,null,null,'0',null,'0',null,'0',null,'0',null,null,null, null,null,'Y', 'Y', 'ncrawford' , CURRENT_DATE);--juror 4


INSERT INTO juror_mod.users (created_by, updated_by,username,email, name, active)
VALUES ('ncrawford','ncrawford','ncrawford','ncrawford@email.gov.uk','Natasha Crawford',true),
       ('ncrawford','ncrawford','lrees','lrees@email.gov.uk','Lewis Rees',true),
       ('ncrawford','ncrawford','kfry','kfry@email.gov.uk','Katherine Fry',true);

insert into juror_mod.user_courts (username, loc_code)
values ('ncrawford', '415'),
       ('lrees', '415'),
       ('kfry', '415');

---- pending juror records for court 415

-- create a pool record owned by 415
INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request, last_update)
VALUES ('415220502', '415', current_date + 10, 5, 5, 'CRO', '415', 'N', current_date);


INSERT INTO juror_mod.pending_juror (juror_number,pool_number,title,last_name,first_name,dob,address_line_1,
address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,m_phone,h_email,
contact_preference,responded,next_date,date_added,mileage,pool_seq,status,is_active,added_by,notes,date_created) VALUES
 ('041500001','415220502','Mr','Smitha','Johna','1990-01-01','1 High Street','Test','Test','Test','Test','TE1 1ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'Q',NULL,NULL,'Notes on record',current_date);

INSERT INTO juror_mod.pending_juror (juror_number,pool_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,m_phone,h_email,contact_preference,responded,next_date,date_added,mileage,pool_seq,status,is_active,added_by,notes,date_created) VALUES
 ('041500002','415220502','Mr','Smithb','Johnb','1990-01-01','2 High Street','Test','Test','Test','Test','TE1 2ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'Q',NULL,NULL,NULL,current_date),
 ('041500003','415220502','Mr','Smithc','Johnc','1990-01-01','3 High Street','Test','Test','Test','Test','TE1 3ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'R',NULL,NULL,'Notes on record 3',current_date),
 ('041500004','415220502','Mr','Smithd','Johnd','1990-01-01','4 High Street','Test','Test','Test','Test','TE1 4ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'A',NULL,NULL,'Notes on record 4',current_date);


------ UnpaidAttendance info

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,trial_number,non_attendance,no_show,misc_description,pay_cash,last_updated_by,created_by,public_transport_total_due,public_transport_total_paid,hired_vehicle_total_due,hired_vehicle_total_paid,motorcycle_total_due,motorcycle_total_paid,car_total_due,car_total_paid,pedal_cycle_total_due,pedal_cycle_total_paid,childcare_total_due,childcare_total_paid,parking_total_due,parking_total_paid,misc_total_due,misc_total_paid,smart_card_due,is_draft_expense,f_audit,sat_on_jury,pool_number,appearance_stage,loss_of_earnings_due,loss_of_earnings_paid,subsistence_due,subsistence_paid,attendance_type,smart_card_paid,travel_time,travel_jurors_taken_by_car,travel_by_car,travel_jurors_taken_by_motorcycle,travel_by_motorcycle,travel_by_bicycle,miles_traveled,food_and_drink_claim_type,"version",expense_rates_id,attendance_audit_number,appearance_confirmed,hide_on_unpaid_expense_and_reports) VALUES
	 ('2025-01-20','586856851','415','10:45:00','16:30:00',NULL,false,NULL,NULL,false,NULL,'micha.alsh1',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,NULL,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY_LONG_TRIAL',NULL,'00:00:00',NULL,NULL,NULL,NULL,NULL,35,'LESS_THAN_OR_EQUAL_TO_10_HOURS',3,NULL,'J10149352',true,false),
	 ('2024-09-09','586856851','415','09:00:00','16:00:00',NULL,false,NULL,NULL,false,NULL,'johin.shull',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,NULL,NULL,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,NULL,NULL,NULL,NULL,32,'LESS_THAN_OR_EQUAL_TO_10_HOURS',4,999999,'P10113974',true,false),
	 ('2024-09-08','472008411','415','09:00:00','16:00:00',NULL,false,NULL,NULL,false,NULL,'johin.shull',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,NULL,NULL,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,NULL,NULL,NULL,NULL,45,'LESS_THAN_OR_EQUAL_TO_10_HOURS',4,999999,'P10113326',true,false),
	 ('2024-09-09','487498307','415','09:00:00','16:00:00',NULL,false,NULL,NULL,false,NULL,'johin.shull',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,0.00,NULL,NULL,true,NULL,NULL,'415220502','EXPENSE_ENTERED',64.95,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,NULL,NULL,NULL,NULL,0,'LESS_THAN_OR_EQUAL_TO_10_HOURS',5,999999,'P10113974',true,false),
	 ('2024-09-09','209092530','415','10:00:00','16:00:00',NULL,false,NULL,NULL,false,NULL,'johin.shull',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,NULL,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,NULL,NULL,NULL,NULL,14,'LESS_THAN_OR_EQUAL_TO_10_HOURS',5,999999,'P10113974',true,false),
	 ('2025-05-19','586856851','415','09:00:00','14:45:00',NULL,false,NULL,'',false,NULL,'Sam.nore2',5.60,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,false,40869063,NULL,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,false,NULL,false,false,0,'LESS_THAN_OR_EQUAL_TO_10_HOURS',10,999999,'P10183598',true,false),
	 ('2025-05-21','586856851','415','10:00:00','16:35:00',NULL,false,NULL,'',false,NULL,'Sam.nore2',5.60,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,false,40869063,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,false,NULL,false,false,0,'LESS_THAN_OR_EQUAL_TO_10_HOURS',14,999999,'J10184684',true,false),
	 ('2025-05-22','586856851','415','10:15:00','16:30:00',NULL,false,NULL,'',false,NULL,'Georg.irnie',5.60,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,false,40869063,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,false,NULL,false,false,0,'LESS_THAN_OR_EQUAL_TO_10_HOURS',8,999999,'J10185126',true,false),
	 ('2025-05-23','586856851','415','10:15:00','15:30:00',NULL,false,NULL,'',false,NULL,'Georg.irnie',5.60,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,false,40869063,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,false,NULL,false,false,0,'LESS_THAN_OR_EQUAL_TO_10_HOURS',8,999999,'J10185386',true,false),
	 ('2025-05-27','586856851','415','10:15:00','15:30:00',NULL,false,NULL,'',false,NULL,'Georg.irnie',5.60,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,false,40869063,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY',NULL,'00:00:00',NULL,false,NULL,false,false,0,'LESS_THAN_OR_EQUAL_TO_10_HOURS',7,999999,'J10185618',true,false),
	 ('2025-05-29','586856851','415','10:15:00','12:00:00',NULL,false,NULL,'',false,NULL,'Georg.irnie',3.00,NULL,0.00,NULL,NULL,NULL,2.83,NULL,NULL,NULL,NULL,NULL,0.00,NULL,NULL,NULL,NULL,false,40868465,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'HALF_DAY_LONG_TRIAL',NULL,'00:00:00',NULL,true,NULL,false,false,9,'LESS_THAN_OR_EQUAL_TO_10_HOURS',7,999999,'J10186427',true,false),
	 ('2025-05-28','586856851','415','10:15:00','13:00:00',NULL,false,NULL,'',false,NULL,'Georg.irnie',0.00,NULL,0.00,NULL,NULL,NULL,5.65,NULL,NULL,NULL,NULL,NULL,2.50,NULL,NULL,NULL,NULL,false,40868465,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY_LONG_TRIAL',NULL,'00:00:00',NULL,true,NULL,false,false,18,'LESS_THAN_OR_EQUAL_TO_10_HOURS',7,999999,'J10186186',true,false),
	 ('2025-03-17','586856851','415','10:00:00','16:00:00',NULL,false,NULL,NULL,false,NULL,'johin.shull',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,NULL,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY_LONG_TRIAL',NULL,'00:00:00',NULL,NULL,NULL,NULL,NULL,35,'LESS_THAN_OR_EQUAL_TO_10_HOURS',3,NULL,'J10166150',true,false),
   ('2025-06-03','586856851','415','10:15:00','15:10:00',NULL,false,NULL,NULL,false,NULL,'Georg.irnie',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,NULL,true,'415220502','EXPENSE_ENTERED',NULL,NULL,5.71,NULL,'FULL_DAY_LONG_TRIAL',NULL,'00:00:00',NULL,NULL,NULL,NULL,NULL,8,'LESS_THAN_OR_EQUAL_TO_10_HOURS',4,NULL,'J10187987',true,false);


----- utilisation stats record

INSERT INTO juror_mod.utilisation_stats (month_start,loc_code,available_days,attendance_days,sitting_days,no_trials,last_update) VALUES
	 ('2025-05-01','415',1429,1158,888,NULL,'2025-06-03 08:14:28.000'),
	 ('2025-04-01','415',1385,1036,813,NULL,'2025-05-01 10:50:08.000'),
	 ('2025-03-01','415',1425,1058,773,NULL,'2025-04-08 14:06:44.000'),
	 ('2025-02-01','415',1362,1055,918,NULL,'2025-02-28 17:04:35.000');
