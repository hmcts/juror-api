
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('001','West Oxfordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('002','Broxtowe',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('003','Eastleigh',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('004','Blackburn',false,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('005','Harrogate',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('006','Folkestone & Hythe',true,'NOT_UPLOADED','previously Shepway',NULL,NULL,NULL),
	 ('007','Bradford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);

INSERT INTO juror_er."user" (username,la_code,active,last_logged_in) VALUES
	 ('test_user1@localauthority1.council.uk','001',true,NULL),
	 ('test_user1@la1.council.uk','001',true,NULL),
	 ('test_user2@localauthority1.council.uk','001',true,NULL),
	 ('test_user1@localauthority2.council.uk','002',true,NULL),
	 ('test_user2@localauthority2.council.uk','002',true,NULL),
	 ('test_user1@localauthority4.council.uk','004',false,NULL);
