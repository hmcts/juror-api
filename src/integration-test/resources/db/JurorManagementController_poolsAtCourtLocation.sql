DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.appearance;

-- CHESTER COURT (415)
-- 415230101 - REQUESTED - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('415', '415230101',current_date - 10, 100, 100, 'CRO', '415', 'Y');

-- CHICHESTER COURT (416)
-- 416230101 - ACTIVE - Civil Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('400', '416230101', current_date - 10, 100, 100, 'CIV', '416', 'N');

-- 416230102 - ACTIVE (NIL POOL) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, nil_pool)
VALUES('416', '416230102',current_date - 10, 0, 0, 'CRO', '416', 'N', true);

-- 416230103 - COMPLETED - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('416', '416230103', current_date - 10, 100, 100, 'CRO', '416', 'N');

-- 416230104 - COMPLETED (NIL POOL) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, nil_pool)
VALUES('416', '416230104', current_date - 10, 0, 0, 'CRO', '416', 'N', true);

-- 416230105 - REQUESTED - High Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('416', '416230105',current_date - 10, 100, 100, 'HGH', '416', 'Y');

-- COVENTRY COURT (417)

-- 417230101 - COMPLETED - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('417', '417230101', current_date - 10, 100, 100, 'CRO', '417', 'N');

-- 417230102 - ACTIVE - Civil Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('400', '417230102', current_date - 10, 100, 100, 'CIV', '417', 'N');

-- 417230103 - NIL POOL - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, nil_pool)
VALUES('400', '417230103', current_date - 10, 0, 0, 'CRO', '417', 'N', true);

-- 417230104 - REQUESTED - Civil Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('417', '417230104', current_date - 10, 100, 100, 'CIV', '417', 'Y');

-- CROYDON COURT (418)

-- 418230101 - ACTIVE (created via pool member transfer) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230101', DATE_TRUNC('day', CURRENT_DATE + 1), 1, NULL, 'CRO', '418', 'N');

-- 418230102 - ACTIVE (created via pool member transfer - still has active pool members) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230102', DATE_TRUNC('day', CURRENT_DATE - 1), 1, NULL, 'CRO', '418', 'N');

-- 418230103 - COMPLETED (created via pool member transfer - no active pool members) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230103', DATE_TRUNC('day', CURRENT_DATE - 1), 1, NULL, 'CRO', '418', 'N');

-- 418230104 - COMPLETED (created via pool member transfer - no pool members) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230104', DATE_TRUNC('day', CURRENT_DATE - 1), 1, NULL, 'CRO', '418', 'N');

-- 418230105 - ACTIVE (Nil Pool) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO,  RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, NIL_POOL)
VALUES('418', '418230105', date_trunc('day', current_date), 0, 0, 'CRO', '418', 'N', true);

-- 418230106 - COMPLETED (Nil Pool) - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, NIL_POOL)
VALUES('418', '418230106', DATE_TRUNC('day', CURRENT_DATE - 1), 0, 0, 'CRO', '418','N', true);

-- 418230107 - REQUESTED - Crown Court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230107', DATE_TRUNC('day', CURRENT_DATE+ 50), 50, 50, 'CRO', '418', 'Y');


-- POOL MEMBERS

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, completion_date) VALUES
('641600001', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641600002', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '2023-02-06'),
('641500001', 'PERSON1', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '2023-01-23'),
('641500002', 'PERSON2', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641500003', 'PERSON3', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641500004', 'PERSON4', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641500005', 'PERSON5', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641500006', 'PERSON6', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641500007', 'PERSON7', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800001', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800002', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800003', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '2023-10-24');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status, on_call) VALUES
('400', '641600001', '416230101', true, '2023-01-23', 2, null),
('416', '641600002', '416230103', true, '2023-01-23', 13, null),
('415', '641500001', '415230101', true, current_date - 10, 13, null),
('415', '641500002', '415230101', true, current_date - 10, 7, null),
('415', '641500003', '415230101', true, current_date - 10, 2, null),
('415', '641500004', '415230101', true, current_date - 10, 2, true),
('415', '641500005', '415230101', true, current_date - 10, 4, null),
('415', '641500006', '415230101', true, current_date - 10, 2, null),
('415', '641500007', '415230101', true, current_date - 10, 2, null),
('418', '641800001', '418230101', true, '2023-10-25', 2, null),
('418', '641800002', '418230102', true, '2023-10-23', 2, null),
('418', '641800003', '418230103', true, '2023-10-23', 13, null);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
	(current_date,'641500003','415230101','415',123456789,'09:30:00',null,'01:12','CHECKED_IN',false);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
	(current_date,'641500006','415230101','415',123456789,'09:30:00',null,'01:12','CHECKED_IN',false);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
	(current_date,'641500005','415230101','415',123456789,'09:30:00',null,'01:12','CHECKED_IN',false);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
	(current_date,'641500007','415230101','415',123456789,'09:30:00',null,'01:12',null,false);
