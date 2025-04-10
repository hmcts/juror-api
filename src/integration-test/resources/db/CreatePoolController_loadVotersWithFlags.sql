DELETE FROM juror_mod.voters;
UPDATE juror_mod.court_location set voters_lock = 0;

INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES
('641500001','1','1',NULL,NULL,'LNAMEONE','FNAMEONE',NULL,NULL,'1 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,1,'Y',NULL),
('641500002','2','2',NULL,NULL,'LNAMETWO','FNAMETWO',NULL,NULL,'2 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,2,'Y',NULL),
('641500003','3','3',NULL,NULL,'LNAMETHREE','FNAMETHREE',NULL,'X','3 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,3,NULL,NULL),
('641500004','4','4',NULL,NULL,'LNAMEFOUR','FNAMEFOUR',NULL,NULL,'4 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,4,NULL,NULL),
('641500005','5','5',NULL,NULL,'LNAMEFIVE','FNAMEFIVE',NULL,NULL,'5 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',TIMESTAMP'2022-09-04 00:00:00.0',NULL,NULL,5,NULL,NULL),
('641500006','6','6',NULL,NULL,'LNAMESIX','FNAMESIX',NULL,NULL,'6 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,6,NULL,NULL),
('641500007','7','7',NULL,NULL,'LNAMESEVEN','FNAMESEVEN',NULL,NULL,'7 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,7,NULL,NULL),
('641500008','8','8',NULL,NULL,'LNAMEEIGHT','FNAMEEIGHT',NULL,NULL,'8 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,8,NULL,NULL),
('641500009','9','9',NULL,NULL,'LNAMENINE','FNAMENINE',NULL,NULL,'9 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',TIMESTAMP'2022-04-17 00:00:00.0',NULL,NULL,9,NULL,NULL),
('641500010','10','10',NULL,NULL,'LNAMEONEZERO','FNAMEONEZERO',NULL,NULL,'10 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,10,NULL,NULL),
('641500011','11','11',NULL,NULL,'LNAMEONEONE','FNAMEONEONE',NULL,NULL,'11 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,11,NULL,NULL),
('641500012','12','12',NULL,NULL,'LNAMEONETWO','FNAMEONETWO',NULL,NULL,'12 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,12,NULL,NULL),
('641500013','13','13',NULL,NULL,'LNAMEONETHREE','FNAMEONETHREE',NULL,'X','13 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,13,NULL,NULL),
('641500014','14','14',NULL,NULL,'LNAMEONEFOUR','FNAMEONEFOUR',NULL,NULL,'14 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',TIMESTAMP'2022-04-03 00:00:00.0',NULL,NULL,14,NULL,NULL),
('641500015','15','15',NULL,NULL,'LNAMEONEFIVE','FNAMEONEFIVE',NULL,NULL,'15 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',TIMESTAMP'2022-04-17 00:00:00.0',NULL,NULL,15,NULL,NULL),
('641500016','16','16',NULL,NULL,'LNAMEONESIX','FNAMEONESIX',NULL,NULL,'16 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,16,NULL,NULL),
('641500020','20','20',NULL,NULL,'LNAMETWOZERO','FNAMETWOZERO',NULL,NULL,'20 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AR',TIMESTAMP'2022-04-17 00:00:00.0',NULL,NULL,20,NULL,NULL),
('641500022','22','22',NULL,NULL,'LNAMETWOTWO','FNAMETWOTWO',TIMESTAMP'1943-04-17 00:00:00.0',NULL,'22 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AR',NULL,NULL,NULL,22,NULL,NULL),
('641500023','23','23',NULL,NULL,'LNAMETWOTHREE','FNAMETWOTHREE',current_date - 6200,NULL,'23 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,23,NULL,NULL);
