delete from juror_mod.pool_history;
delete from juror_mod.pool_comments;
delete from juror_mod.juror_pool;
delete from juror_mod.pool;

INSERT INTO JUROR_MOD.POOL (OWNER,POOL_NO,RETURN_DATE,TOTAL_NO_REQUIRED,NO_REQUESTED,POOL_TYPE,LOC_CODE,NEW_REQUEST,LAST_UPDATE,ADDITIONAL_SUMMONS,ATTEND_TIME) VALUES
('400','123456789',TIMESTAMP'2022-06-27 00:00:00.0',10,10,'CRO','457','T',TIMESTAMP'2022-06-16 13:34:22.0',NULL,NULL);

