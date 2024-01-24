delete from juror_mod.contact_log;
delete from juror_mod.pool_history;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
delete from juror_mod.pool_comments;
delete from juror_mod.bulk_print_data;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

INSERT INTO JUROR_MOD.POOL (OWNER,POOL_NO,RETURN_DATE,TOTAL_NO_REQUIRED,NO_REQUESTED,POOL_TYPE,LOC_CODE,NEW_REQUEST,LAST_UPDATE,ADDITIONAL_SUMMONS,ATTEND_TIME) VALUES
	 ('400','415221201',TIMESTAMP'2022-12-04 00:00:00.0',5,5,'CRO','415','N',TIMESTAMP'2022-10-02 09:22:09.0',NULL,NULL);




