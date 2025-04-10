DELETE FROM juror_mod.voters;
DELETE FROM JUROR_MOD.CORONER_POOL_DETAIL;
DELETE FROM JUROR_MOD.CORONER_POOL;

INSERT INTO JUROR_MOD.CORONER_POOL (COR_POOL_NO,COR_NAME,EMAIL,PHONE,COR_COURT_LOC,COR_REQUEST_DT,COR_SERVICE_DT,COR_NO_REQUESTED)
VALUES ('923040001','Coroners Name','emailof@coroner.gov.uk','0207 12341234','415',TIMESTAMP'2023-04-03 00:00:00.0',TIMESTAMP'2023-04-03 00:00:00.0',100);

INSERT INTO JUROR_MOD.CORONER_POOL (COR_POOL_NO,COR_NAME,EMAIL,PHONE,COR_COURT_LOC,COR_REQUEST_DT,COR_SERVICE_DT,COR_NO_REQUESTED)
VALUES ('923040002','Coroners Name','emailofsecond@coroner.gov.uk','0207 12341236','415',TIMESTAMP'2023-04-03 00:00:00.0',TIMESTAMP'2023-04-03 00:00:00.0',30);

INSERT INTO juror_mod.voters (LOC_CODE,PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ADDRESS6,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('415','641500001','1','1',NULL,NULL,'LNAMEONE','FNAMEONE',NULL,NULL,'1 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,1,NULL,NULL),
('415','641500002','2','2',NULL,NULL,'LNAMETWO','FNAMETWO',NULL,NULL,'2 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,2,NULL,NULL),
('415','641500003','3','3',NULL,NULL,'LNAMETHREE','FNAMETHREE',NULL,NULL,'3 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,3,NULL,NULL),
('415','641500004','4','4',NULL,NULL,'LNAMEFOUR','FNAMEFOUR',NULL,NULL,'4 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,4,NULL,NULL),
('415','641500005','5','5',NULL,NULL,'LNAMEFIVE','FNAMEFIVE',NULL,NULL,'5 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,5,NULL,NULL),
('415','641500006','6','6',NULL,NULL,'LNAMESIX','FNAMESIX',NULL,NULL,'6 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,6,NULL,NULL),
('415','641500007','7','7',NULL,NULL,'LNAMESEVEN','FNAMESEVEN',NULL,NULL,'7 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,7,NULL,NULL),
('415','641500008','8','8',NULL,NULL,'LNAMEEIGHT','FNAMEEIGHT',NULL,NULL,'8 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,8,NULL,NULL),
('415','641500009','9','9',NULL,NULL,'LNAMENINE','FNAMENINE',NULL,NULL,'9 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,9,NULL,NULL),
('415','641500010','10','10',NULL,NULL,'LNAMEONEZERO','FNAMEONEZERO',NULL,NULL,'10 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,10,NULL,NULL),
('415','641500011','11','11',NULL,NULL,'LNAMEONEONE','FNAMEONEONE',NULL,NULL,'11 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,11,NULL,NULL),
('415','641500012','12','12',NULL,NULL,'LNAMEONETWO','FNAMEONETWO',NULL,NULL,'12 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,12,NULL,NULL),
('415','641500013','13','13',NULL,NULL,'LNAMEONETHREE','FNAMEONETHREE',NULL,NULL,'13 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,13,NULL,NULL),
('415','641500014','14','14',NULL,NULL,'LNAMEONEFOUR','FNAMEONEFOUR',NULL,NULL,'14 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,14,NULL,NULL),
('415','641500015','15','15',NULL,NULL,'LNAMEONEFIVE','FNAMEONEFIVE',NULL,NULL,'15 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,15,NULL,NULL),
('415','641500016','16','16',NULL,NULL,'LNAMEONESIX','FNAMEONESIX',NULL,NULL,'16 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,16,NULL,NULL),
('415','641500017','17','17',NULL,NULL,'LNAMEONESEVEN','FNAMEONESEVEN',NULL,NULL,'17 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH3 2AR',NULL,NULL,NULL,17,NULL,NULL),
('415','641500018','18','18',NULL,NULL,'LNAMEONEEIGHT','FNAMEONEEIGHT',NULL,NULL,'18 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH3 2AR',NULL,NULL,NULL,18,NULL,NULL),
('415','641500019','19','19',NULL,NULL,'LNAMEONENINE','FNAMEONENINE',NULL,NULL,'19 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH3 2AR',NULL,NULL,NULL,19,NULL,NULL),
('415','641500020','20','20',NULL,NULL,'LNAMETWOZERO','FNAMETWOZERO',NULL,NULL,'20 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH3 2AR',TIMESTAMP'2022-04-17 00:00:00.0',NULL,NULL,20,NULL,NULL),
('415','641500021','21','21',NULL,NULL,'LNAMETWOONE','FNAMETWOONE',NULL,NULL,'21 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,21,NULL,NULL),
('415','641500022','22','22',NULL,NULL,'LNAMETWOTWO','FNAMETWOTWO',TIMESTAMP'1943-04-17 00:00:00.0',NULL,'22 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH3 2AR',NULL,NULL,NULL,22,NULL,NULL),
('415','641500023','23','23',NULL,NULL,'LNAMETWOTHREE','FNAMETWOTHREE',NULL,NULL,'23 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,23,NULL,NULL),
('415','641500921','921','921',NULL,NULL,'LNAMENINETWOONE','FNAMENINETWOONE',NULL,NULL,'921 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,921,NULL,NULL),
('415','641500922','922','922',NULL,NULL,'LNAMENINETWOTWO','FNAMENINETWOTWO',NULL,NULL,'922 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,922,NULL,NULL),
('415','641500923','923','923',NULL,NULL,'LNAMENINETWOTHREE','FNAMENINETWOTHREE',NULL,NULL,'923 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,923,NULL,NULL),
('415','641500924','924','924',NULL,NULL,'LNAMENINETWOFOUR','FNAMENINETWOFOUR',NULL,NULL,'924 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,924,NULL,NULL),
('415','641500925','925','925',NULL,NULL,'LNAMENINETWOFIVE','FNAMENINETWOFIVE',NULL,NULL,'925 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH6 7TY',NULL,NULL,NULL,925,NULL,NULL),
('415','641500926','926','926',NULL,NULL,'LNAMENINETWOSIX','FNAMENINETWOSIX',NULL,NULL,'926 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH6 7TY',NULL,NULL,NULL,926,NULL,NULL),
('415','641500927','927','927',NULL,NULL,'LNAMENINETWOSEVEN','FNAMENINETWOSEVEN',NULL,NULL,'927 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH6 7TY',NULL,NULL,NULL,927,NULL,NULL),
('415','641500928','928','928',NULL,NULL,'LNAMENINETWOEIGHT','FNAMENINETWOEIGHT',NULL,NULL,'928 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,928,NULL,NULL),
('415','641500929','929','929',NULL,NULL,'LNAMENINETWONINE','FNAMENINETWONINE',NULL,NULL,'929 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,929,NULL,NULL),
('415','641500930','930','930',NULL,NULL,'LNAMENINETHREEZERO','FNAMENINETHREEZERO',NULL,NULL,'930 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,930,NULL,NULL),
('415','641500931','931','931',NULL,NULL,'LNAMENINETHREEONE','FNAMENINETHREEONE',NULL,NULL,'931 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,931,NULL,NULL),
('415','641500932','932','932',NULL,NULL,'LNAMENINETHREETWO','FNAMENINETHREETWO',NULL,NULL,'932 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,932,NULL,NULL),
('415','641500933','933','933',NULL,NULL,'LNAMENINETHREETHREE','FNAMENINETHREETHREE',NULL,NULL,'933 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,933,NULL,NULL),
('415','641500934','934','934',NULL,NULL,'LNAMENINETHREEFOUR','FNAMENINETHREEFOUR',NULL,NULL,'934 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,934,NULL,NULL),
('415','641500935','935','935',NULL,NULL,'LNAMENINETHREEFIVE','FNAMENINETHREEFIVE',NULL,NULL,'935 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,935,NULL,NULL),
('415','641500936','936','936',NULL,NULL,'LNAMENINETHREESIX','FNAMENINETHREESIX',NULL,NULL,'936 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,936,NULL,NULL),
('415','641500937','937','937',NULL,NULL,'LNAMENINETHREESEVEN','FNAMENINETHREESEVEN',NULL,NULL,'937 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,937,NULL,NULL),
('415','641500938','938','938',NULL,NULL,'LNAMENINETHREEEIGHT','FNAMENINETHREEEIGHT',NULL,NULL,'938 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,938,NULL,NULL),
('415','641500939','939','939',NULL,NULL,'LNAMENINETHREENINE','FNAMENINETHREENINE',NULL,NULL,'939 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,939,NULL,NULL),
('415','641500940','940','940',NULL,NULL,'LNAMENINEFOURZERO','FNAMENINEFOURZERO',NULL,NULL,'940 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,940,NULL,NULL),
('415','641500941','941','941',NULL,NULL,'LNAMENINEFOURONE','FNAMENINEFOURONE',NULL,NULL,'941 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,941,NULL,NULL),
('416','641600071','71','71',NULL,NULL,'LNAMESEVENONE','FNAMESEVENONE',NULL,NULL,'71 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'PO19 1SX',NULL,NULL,NULL,71,NULL,NULL),
('416','641600070','70','70',NULL,NULL,'LNAMESEVENZERO','FNAMESEVENZERO',NULL,NULL,'70 STREET NAME','ANYTOWN',NULL,NULL,NULL,NULL,'PO19 1SX',NULL,NULL,NULL,70,NULL,NULL);
