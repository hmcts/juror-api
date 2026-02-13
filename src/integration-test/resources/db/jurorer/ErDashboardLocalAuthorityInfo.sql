delete from juror_er.reminder_history;
delete from juror_er.file_uploads;
delete from juror_er.user;
delete from juror_er.local_authority;
delete from juror_er.deadline;


INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,email_request_status,email_request_sent,updated_by,last_updated) VALUES
	 ('001','West Oxfordshire',true,'UPLOADED','some test notes',NULL,'SENT', current_timestamp - interval '10 days',NULL,NULL),
	 ('002','Broxtowe',true,'UPLOADED',NULL,NULL,NULL,NULL,NULL,NULL),
	 ('003','Eastleigh',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL,NULL,NULL),
	 ('004','Blackburn',false,'NOT_UPLOADED',NULL,NULL,NULL,NULL,NULL,NULL),
	 ('005','Harrogate',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL,NULL,NULL),
	 ('006','Folkestone & Hythe',true,'NOT_UPLOADED','previously Shepway',NULL,NULL,NULL,NULL,NULL),
	 ('007','Bradford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO juror_er.user (username,la_code,active,last_logged_in) VALUES
	 ('test_user1@localauthority1.council.uk','001',true,current_timestamp - interval '1 day'),
	 ('test_user2@localauthority1.council.uk','001',true,NULL),
	 ('test_user1@localauthority2.council.uk','002',true,NULL),
	 ('test_user2@localauthority2.council.uk','002',true,NULL),
	 ('test_user1@localauthority4.council.uk','004',false,NULL);

INSERT INTO juror_er.file_uploads (la_code,la_username,filename,file_format,file_size_bytes,other_information,upload_date) VALUES
	 ('002','test_user1@localauthority2.council.uk','002_er_data2.csv','CSV',11000000,'this is the third file to be uploaded',current_date - interval '2 days'),
	 ('001','test_user1@localauthority1.council.uk','001_er_data.xls','XLS',10040000,'this is the first file to be uploaded',current_date - interval '1 week'),
	 ('001','test_user1@localauthority1.council.uk','001_er_data2.xls','XLS',10000000,'this is the second file to be uploaded',current_date - interval '5 days'),
	 ('001','test_user1@localauthority1.council.uk','001_er_data.txt','TXT',10040000,'this is the nth file to be uploaded',current_date - interval '25 weeks'),
	 ('001','test_user1@localauthority1.council.uk','001_er_data2.xls','XLS',10000000,'this is the second file to be uploaded',current_date - interval '1 week');

INSERT INTO juror_er.reminder_history (id,la_code,sent_by,time_sent) VALUES
	 (1,'001', 'bureau_user', current_timestamp - interval '2 days'),
	 (2,'002', 'bureau_user', current_timestamp - interval '1 day'),
	 (3,'003', 'bureau_user', current_timestamp - interval '3 days'),
	 (4,'004', 'bureau_user', current_timestamp - interval '4 days'),
	 (5,'005', 'bureau_user', current_timestamp - interval '5 days');
