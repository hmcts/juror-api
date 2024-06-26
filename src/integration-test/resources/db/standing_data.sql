-- Standing data.
INSERT INTO juror_mod.APP_SETTING (SETTING, VALUE) VALUES ('URGENCY_DAYS', '10');
INSERT INTO juror_mod.APP_SETTING (SETTING, VALUE) VALUES ('SLA_OVERDUE_DAYS', '5');

INSERT INTO JUROR_MOD.SYSTEM_PARAMETER(SP_ID, SP_DESC, SP_VALUE) values('100','Upper Age Limit', '76')
ON CONFLICT(SP_ID)
DO UPDATE SET SP_DESC = 'Upper Age Limit', SP_VALUE = '76';

INSERT INTO JUROR_MOD.SYSTEM_PARAMETER(SP_ID, SP_DESC, SP_VALUE) values('101','Lower Age Limit', '18')
ON CONFLICT(SP_ID)
DO UPDATE SET SP_DESC = 'Lower Age Limit', SP_VALUE = '18';

--MERGE INTO JUROR_MOD.SYSTEM_PARAMETER USING select '100' ON ( SP_ID = '100' )
--WHEN MATCHED THEN UPDATE SET SP_DESC = 'Upper Age Limit', SP_VALUE = '76' WHERE SP_ID = '100'
--WHEN NOT MATCHED THEN INSERT (SP_ID, SP_DESC, SP_VALUE) VALUES ('100', 'Upper Age Limit', '76');

--MERGE INTO JUROR_MOD.SYSTEM_PARAMETER USING select '101' ON ( SP_ID = '101' )
--WHEN MATCHED THEN UPDATE SET SP_DESC = 'Lower Age Limit', SP_VALUE = '18' WHERE SP_ID = '101'
--WHEN NOT MATCHED THEN INSERT (SP_ID, SP_DESC, SP_VALUE) VALUES ('101', 'Lower Age Limit', '18');

-- Auto user
INSERT INTO juror_mod.users (created_by, updated_by,username, name, active, email, user_type)
VALUES ('AUTO', 'AUTO','AUTO','AUTO', true, 'AUTO@hmcts.gov.uk', 'SYSTEM');
insert into juror_mod.user_courts (username, loc_code)
values ('AUTO', '400');