INSERT INTO JUROR.UNIQUE_POOL (POOL_NO,NEXT_DATE,NO_REQUESTED,POOL_TOTAL,REG_SPC,RETURN_DATE,OWNER) VALUES (101,CURRENT_DATE,20,300,'N',CURRENT_DATE,'400');
INSERT INTO JUROR.UNIQUE_POOL (POOL_NO,NEXT_DATE,NO_REQUESTED,POOL_TOTAL,REG_SPC,RETURN_DATE,ATTEND_TIME,OWNER) VALUES (102, CURRENT_DATE,20,300,'N',CURRENT_DATE,(SELECT CURRENT_DATE + 1/24),'400');