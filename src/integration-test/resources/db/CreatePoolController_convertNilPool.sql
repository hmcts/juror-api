delete from juror_mod.pool_history;
delete from juror_mod.pool_comments;
delete from juror_mod.bulk_print_data;
DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
delete from juror_mod.juror_pool;
delete from juror_mod.pool;

INSERT INTO JUROR_MOD.POOL (OWNER,POOL_NO,RETURN_DATE,TOTAL_NO_REQUIRED,NO_REQUESTED,POOL_TYPE,LOC_CODE,NEW_REQUEST,LAST_UPDATE,ADDITIONAL_SUMMONS,ATTEND_TIME) VALUES
 ('400','416221201',TIMESTAMP'2022-09-04 00:00:00.0',5,5,'CRO','415','N',TIMESTAMP'2022-02-02 09:22:09.0',NULL,TIMESTAMP'2022-09-04 09:00:00.0');

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, address_line_1, address_line_4, postcode, responded)
VALUES ('641600090', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '542 STREET NAME', 'ANYTOWN', 'PO19 1SX', true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES('416', '641600090', '416221201', true, 7);