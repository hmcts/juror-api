DELETE FROM juror_er.file_uploads;
DELETE FROM juror_er."user";
DELETE FROM juror_er.local_authority;
DELETE FROM juror_er.deadline;

INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
    ('001','West Oxfordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
    ('002','Broxtowe',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
    ('003','Eastleigh',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
    ('004','Blackburn',false,'NOT_UPLOADED',NULL,NULL,NULL,NULL);

-- Use usernames that match what the tests expect (.council.uk)
INSERT INTO juror_er."user" (username,la_code,active,last_logged_in) VALUES
    ('test_user1@la1.council.uk','001',true,NULL),
    ('test_user2@la1.council.uk','001',true,NULL),
    ('test_user1@la2.council.uk','002',true,NULL),
    ('test_user2@la2.council.uk','002',true,NULL),
    ('test_user1@la3.council.uk','003',true,NULL),
    ('test_user1@la4.council.uk','004',false,NULL);

INSERT INTO juror_er.file_uploads (la_code,la_username,filename,file_format,file_size_bytes,other_information,upload_date) VALUES
    ('001','test_user1@la1.council.uk','001_er_data.xls','XLS',10040000,'this is the first file to be uploaded',current_date - interval '19 days'),
    ('001','test_user1@la1.council.uk','001_er_data2.xls','XLS',10000000,'this is the second file to be uploaded',current_date - interval '15 days'),
    ('001','test_user1@la1.council.uk','001_er_data.txt','TXT',10040000,'this is the nth file to be uploaded',current_date - interval '10 days'),
    ('001','test_user1@la1.council.uk','001_er_data3.xls','XLS',10000000,'this is the second file to be uploaded',current_date - interval '20 days'),
    ('001','test_user1@la1.council.uk','001_er_data4.xls','XLS',10000000,'this is the fifth file to be uploaded',current_date - interval '5 days'),
    ('002','test_user1@la2.council.uk','002_er_data2.csv','CSV',11000000,'this is the third file to be uploaded',current_date - interval '1 day');

INSERT INTO juror_er.deadline (id,deadline_date,upload_start_date,updated_by,last_updated) VALUES
    (1,current_date + interval '6 weeks',current_date - interval '6 weeks',NULL,NULL);
