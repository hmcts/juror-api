INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('641500019','19','LNAMEONENINE','FNAMEONENINE',NULL,'19 STREET NAME','ANYTOWN','CH3 2AR','N','COURT_USER',NULL,0,NULL),
	 ('641500004','4','LNAMEFOUR','FNAMEFOUR',NULL,'4 STREET NAME','ANYTOWN','CH1 2AN','N','COURT_USER',NULL,0,NULL),
	 ('641500011','11','LNAMEONEONE','FNAMEONEONE',NULL,'11 STREET NAME','ANYTOWN','CH2 2AB','N','COURT_USER',NULL,0,NULL),
	 ('641500013','13','LNAMEONETHREE','FNAMEONETHREE',NULL,'13 STREET NAME','ANYTOWN','CH2 2AB','N','COURT_USER',NULL,0,NULL),
	 ('641500016','16','LNAMEONESIX','FNAMEONESIX',NULL,'16 STREET NAME','ANYTOWN','CH1 2AN','N','COURT_USER',NULL,0,NULL),
	 ('641500007','7','LNAMESEVEN','FNAMESEVEN',NULL,'7 STREET NAME','ANYTOWN','CH1 2AN','N','COURT_USER',NULL,0,NULL),
	 ('641500001','1','LNAMEONE','FNAMEONE',NULL,'1 STREET NAME','ANYTOWN','CH1 2AN','N','COURT_USER',NULL,0,NULL),
	 ('641500017','17','LNAMEONESEVEN','FNAMEONESEVEN',NULL,'17 STREET NAME','ANYTOWN','CH3 2AR','N','COURT_USER',NULL,0,NULL),
	 ('641500002','2','LNAMETWO','FNAMETWO',NULL,'2 STREET NAME','ANYTOWN','CH1 2AN','N','COURT_USER',NULL,0,NULL),
	 ('641500018','18','LNAMEONEEIGHT','FNAMEONEEIGHT',NULL,'18 STREET NAME','ANYTOWN','CH3 2AR','N','COURT_USER',NULL,0,NULL),
	 ('641500003','3','LNAMETHREE','FNAMETHREE',NULL,'3 STREET NAME','ANYTOWN','CH1 2AN','N','COURT_USER',NULL,0,NULL),
	 ('641500021','21','LNAMETWOONE','FNAMETWOONE',NULL,'21 STREET NAME','ANYTOWN','CH1 2AN','N','COURT_USER',NULL,0,NULL);

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','641500019','415221001','COURT_USER',1,'Y','01','415','N'),
	 ('400','641500004','415221001','COURT_USER',1,'Y','01','415','N'),
	 ('400','641500011','415221001','COURT_USER',1,'Y','01','415','N'),
	 ('400','641500013','415221001','COURT_USER',1,'Y','01','415','N'),
	 ('400','641500016','415221001','COURT_USER',1,'Y','01','415','N'),
	 ('400','641500007','415221001','COURT_USER',1,'Y','01','415','N'),
	 ('400','641500001','415221001','COURT_USER',10,'Y','01','415','N'),
	 ('400','641500017','415221001','COURT_USER',7,'Y','01','415','N'),
	 ('400','641500002','415221001','COURT_USER',5,'Y','01','415','N'),
	 ('400','641500018','415221001','COURT_USER',2,'Y','01','415','N'),
	 ('400','641500003','415221001','COURT_USER',2,'Y','01','415','N'),
	 ('400','641500021','415221001','COURT_USER',11,'Y','01','415','N');