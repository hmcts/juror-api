DELETE FROM JUROR.POOL;

INSERT INTO JUROR.POOL
(OWNER, PART_NO, POOL_NO, POLL_NUMBER, LNAME, FNAME, DOB, address, address4, zip, REG_SPC, RET_DATE, RESPONDED, USER_EDTQ, STATUS, IS_ACTIVE, NO_DEF_POS, POOL_SEQ, POOL_TYPE, LOC_CODE, ON_CALL, READ_ONLY, NOTIFICATIONS)
VALUES('415', '123456789', '415220502', '543', 'LNAMEFIVEFOURTHREE', 'FNAMEFIVEFOURTHREE', TIMESTAMP'1989-03-31 00:00:00.0', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', 'R', TIMESTAMP '2022-05-03 00:00:00.000000', 'Y', 'BUREAU_USER_1', 2, 'Y', 0, '0109', 'CRO', '415', 'N', 'N', 0);