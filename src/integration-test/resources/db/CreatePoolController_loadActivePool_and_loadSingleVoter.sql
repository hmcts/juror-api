DELETE FROM JUROR_MOD.VOTERS;

Insert into juror_mod.court_catchment_area (POSTCODE,LOC_CODE) values ('CH1','415');

INSERT INTO JUROR_MOD.VOTERS (PART_NO,REGISTER_LETT,POLL_NUMBER,NEW_MARKER,TITLE,LNAME,FNAME,DOB,FLAGS,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,zip,DATE_SELECTED1,DATE_SELECTED2,DATE_SELECTED3,REC_NUM,PERM_DISQUAL,SOURCE_ID)
VALUES ('641500001','1','1',NULL,NULL,'LNAMEONE','FNAMEONE',NULL,NULL,'1 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,1,NULL,NULL);

delete from juror_mod.pool_history;
delete from juror_mod.pool_comments;
delete from juror_mod.juror_pool;
delete from juror_mod.pool;

INSERT INTO JUROR_MOD.POOL (OWNER,POOL_NO,RETURN_DATE,TOTAL_NO_REQUIRED,NO_REQUESTED,POOL_TYPE,LOC_CODE,NEW_REQUEST,LAST_UPDATE,ADDITIONAL_SUMMONS,ATTEND_TIME) VALUES
('400','415221201',TIMESTAMP'2022-12-04 00:00:00.0',5,5,'CRO','415','N',TIMESTAMP'2022-10-02 09:22:09.0',NULL,TIMESTAMP'2022-12-04 09:00:00.0');
