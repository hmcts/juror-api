DELETE FROM juror_mod.voters;
DELETE FROM juror_mod.coroner_pool_detail;
DELETE FROM juror_mod.coroner_pool;

INSERT INTO juror_mod.coroner_pool (COR_POOL_NO,COR_NAME,EMAIL,PHONE,COR_COURT_LOC,COR_REQUEST_DT,COR_SERVICE_DT,
COR_NO_REQUESTED)
VALUES ('923040002','Coroners Name','emailofsecond@coroner.gov.uk','0207 12341236','415',TIMESTAMP'2023-04-03 00:00:00.0',TIMESTAMP'2023-04-03 00:00:00.0',30);

INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,
ADDRESS_LINE_2,ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500004',NULL,'FNAMEFOUR','LNAMEFOUR','4 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,
ADDRESS_LINE_2,ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500006',NULL,'FNAMESIX','LNAMESIX','6 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500021',NULL,'FNAMETWOONE','LNAMETWOONE','21 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500002',NULL,'FNAMETWO','LNAMETWO','2 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500016',NULL,'FNAMEONESIX','LNAMEONESIX','16 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500005',NULL,'FNAMEFIVE','LNAMEFIVE','5 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500008',NULL,'FNAMEEIGHT','LNAMEEIGHT','8 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500023',NULL,'FNAMETWOTHREE','LNAMETWOTHREE','23 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500007',NULL,'FNAMESEVEN','LNAMESEVEN','7 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500001',NULL,'FNAMEONE','LNAMEONE','1 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500015',NULL,'FNAMEONEFIVE','LNAMEONEFIVE','15 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');
INSERT INTO juror_mod.coroner_pool_detail (COR_POOL_NO,JUROR_NUMBER,TITLE,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,
ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,POSTCODE)
VALUES ('923040002','641500003',NULL,'FNAMETHREE','LNAMETHREE','3 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN');

INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501001','1001','1001',NULL,NULL,'LNAMEONE','FNAMEONE',NULL,NULL,'1 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,1,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501002','2001','2001',NULL,NULL,'LNAMETWO','FNAMETWO',NULL,NULL,'2 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,2,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501003','3001','3001',NULL,NULL,'LNAMETHREE','FNAMETHREE',NULL,NULL,'3 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,3,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501004','4001','4001',NULL,NULL,'LNAMEFOUR','FNAMEFOUR',NULL,NULL,'4 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,4,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501005','5001','5001',NULL,NULL,'LNAMEFIVE','FNAMEFIVE',NULL,NULL,'5 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,5,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501006','6001','6001',NULL,NULL,'LNAMESIX','FNAMESIX',NULL,NULL,'6 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,6,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501007','7001','7001',NULL,NULL,'LNAMESEVEN','FNAMESEVEN',NULL,NULL,'7 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,7,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501008','8001','8001',NULL,NULL,'LNAMEEIGHT','FNAMEEIGHT',NULL,NULL,'8 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,8,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501009','9001','9001',NULL,NULL,'LNAMENINE','FNAMENINE',NULL,NULL,'9 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,9,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501010','10001','10001',NULL,NULL,'LNAMEONEZERO','FNAMEONEZERO',NULL,NULL,'10 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,10,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501011','11001','11001',NULL,NULL,'LNAMEONEONE','FNAMEONEONE',NULL,NULL,'11 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,11,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501012','12001','12001',NULL,NULL,'LNAMEONETWO','FNAMEONETWO',NULL,NULL,'12 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,12,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501013','13001','13001',NULL,NULL,'LNAMEONETHREE','FNAMEONETHREE',NULL,NULL,'13 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,13,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501014','14001','14001',NULL,NULL,'LNAMEONEFOUR','FNAMEONEFOUR',NULL,NULL,'14 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,14,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501015','15001','15001',NULL,NULL,'LNAMEONEFIVE','FNAMEONEFIVE',NULL,NULL,'15 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,15,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501016','16001','16001',NULL,NULL,'LNAMEONESIX','FNAMEONESIX',NULL,NULL,'16 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,16,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501017','17001','17001',NULL,NULL,'LNAMEONESEVEN','FNAMEONESEVEN',NULL,NULL,'17 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH3 2AR',NULL,NULL,NULL,17,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501018','18001','18001',NULL,NULL,'LNAMEONEEIGHT','FNAMEONEEIGHT',NULL,NULL,'18 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH3 2AR',NULL,NULL,NULL,18,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501019','19001','19001',NULL,NULL,'LNAMEONENINE','FNAMEONENINE',NULL,NULL,'19 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH3 2AR',NULL,NULL,NULL,19,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641501020','20001','20001',NULL,NULL,'LNAMETWOZERO','FNAMETWOZERO',NULL,NULL,'20 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH3 2AR',TIMESTAMP'2022-04-17 00:00:00.0',NULL,NULL,20,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500921','921','921',NULL,NULL,'LNAMENINETWOONE','FNAMENINETWOONE',NULL,NULL,'921 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,921,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500922','922','922',NULL,NULL,'LNAMENINETWOTWO','FNAMENINETWOTWO',NULL,NULL,'922 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,922,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500923','923','923',NULL,NULL,'LNAMENINETWOTHREE','FNAMENINETWOTHREE',NULL,NULL,'923 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,923,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500924','924','924',NULL,NULL,'LNAMENINETWOFOUR','FNAMENINETWOFOUR',NULL,NULL,'924 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,924,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500925','925','925',NULL,NULL,'LNAMENINETWOFIVE','FNAMENINETWOFIVE',NULL,NULL,'925 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH6 7TY',NULL,NULL,NULL,925,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500926','926','926',NULL,NULL,'LNAMENINETWOSIX','FNAMENINETWOSIX',NULL,NULL,'926 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH6 7TY',NULL,NULL,NULL,926,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500927','927','927',NULL,NULL,'LNAMENINETWOSEVEN','FNAMENINETWOSEVEN',NULL,NULL,'927 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH6 7TY',NULL,NULL,NULL,927,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500928','928','928',NULL,NULL,'LNAMENINETWOEIGHT','FNAMENINETWOEIGHT',NULL,NULL,'928 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,928,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500929','929','929',NULL,NULL,'LNAMENINETWONINE','FNAMENINETWONINE',NULL,NULL,'929 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,929,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500930','930','930',NULL,NULL,'LNAMENINETHREEZERO','FNAMENINETHREEZERO',NULL,NULL,'930 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,930,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500931','931','931',NULL,NULL,'LNAMENINETHREEONE','FNAMENINETHREEONE',NULL,NULL,'931 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,931,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500932','932','932',NULL,NULL,'LNAMENINETHREETWO','FNAMENINETHREETWO',NULL,NULL,'932 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,932,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500933','933','933',NULL,NULL,'LNAMENINETHREETHREE','FNAMENINETHREETHREE',NULL,NULL,'933 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,933,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500934','934','934',NULL,NULL,'LNAMENINETHREEFOUR','FNAMENINETHREEFOUR',NULL,NULL,'934 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,934,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500935','935','935',NULL,NULL,'LNAMENINETHREEFIVE','FNAMENINETHREEFIVE',NULL,NULL,'935 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,935,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500936','936','936',NULL,NULL,'LNAMENINETHREESIX','FNAMENINETHREESIX',NULL,NULL,'936 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,936,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500937','937','937',NULL,NULL,'LNAMENINETHREESEVEN','FNAMENINETHREESEVEN',NULL,NULL,'937 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,937,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500938','938','938',NULL,NULL,'LNAMENINETHREEEIGHT','FNAMENINETHREEEIGHT',NULL,NULL,'938 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,938,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500939','939','939',NULL,NULL,'LNAMENINETHREENINE','FNAMENINETHREENINE',NULL,NULL,'939 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,939,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500940','940','940',NULL,NULL,'LNAMENINEFOURZERO','FNAMENINEFOURZERO',NULL,NULL,'940 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,940,NULL,NULL);
INSERT INTO juror_mod.voters (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ZIP,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500941','941','941',NULL,NULL,'LNAMENINEFOURONE','FNAMENINEFOURONE',NULL,NULL,'941 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH4 7TQ',NULL,NULL,NULL,941,NULL,NULL);
