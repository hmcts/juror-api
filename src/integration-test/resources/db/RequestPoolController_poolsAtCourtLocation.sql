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
VALUES('418', '418230101',  CURRENT_DATE - 10, 10, 10, 'CRO', '418', 'N');

INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230102', CURRENT_DATE - 10, 10, 10, 'CRO', '418', 'N');

INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230103', CURRENT_DATE - 10, 10, 10, 'CRO', '418', 'N');

INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('419', '419230101', CURRENT_DATE - 10, 10, 10, 'CRO', '419', 'N');

-- POOL MEMBERS

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, completion_date) VALUES
('641600001', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641600002', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '2023-02-06'),
('641700001', 'PERSON1', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '2023-01-23'),
('641700002', 'PERSON2', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700003', 'PERSON3', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700004', 'PERSON4', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700005', 'PERSON5', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700006', 'PERSON6', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700007', 'PERSON7', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700008', 'PERSON8', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700009', 'PERSON9', 'TEST9', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700010', 'PERSON10', 'TEST10', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700011', 'PERSON11', 'TEST11', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700012', 'PERSON12', 'TEST12', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700013', 'PERSON13', 'TEST13', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700014', 'PERSON14', 'TEST14', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800001', 'PERSON1', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800002', 'PERSON2', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800003', 'PERSON3', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800004', 'PERSON4', 'TEST4', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800005', 'PERSON5', 'TEST5', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641900001', 'PERSON1', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status, on_call) VALUES
('400', '641600001', '416230101', true, '2023-01-23', 2, false),
('416', '641600002', '416230103', true, '2023-01-23', 13, false),
('417', '641700001', '417230101', true, '2023-01-09', 13, false),
('417', '641700002', '417230101', true, '2023-01-09', 7, false),
('417', '641700003', '417230101', true, '2023-01-09', 2, false),
('417', '641700004', '417230101', true, '2023-01-09', 2, true),
('417', '641700005', '417230101', true, '2023-01-09', 4, false),
('417', '641700006', '417230101', true, '2023-01-09', 2, false),
('417', '641700007', '417230101', true, '2023-01-09', 2, false),
('417', '641700008', '417230101', true, '2023-01-09', 2, false),
('417', '641700009', '417230101', true, '2023-01-09', 3, false),
('417', '641700010', '417230101', true, '2023-01-09', 3, false),
('417', '641700011', '417230101', true, '2023-01-09', 4, false),
('417', '641700012', '417230101', true, '2023-01-09', 4, false),
('417', '641700013', '417230101', true, '2023-01-09', 4, false),
('417', '641700014', '417230101', true, '2023-01-09', 3, false),
('418', '641800001', '418230101', true, '2023-10-25', 2, false),
('418', '641800002', '418230102', true, '2023-10-23', 2, false),
('418', '641800003', '418230103', true, '2023-10-23', 2, false),
('418', '641800004', '418230102', true, '2023-10-23', 3, false),
('418', '641800005', '418230103', true, '2023-10-23', 4, false),
('419', '641900001', '419230101', true, '2023-10-23', 2, true);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
	(current_date,'641700003','417230101','417',123456789,'09:30:00',null,'01:12','CHECKED_IN',false),
	(current_date,'641700006','417230101','417',123456789,'09:30:00',null,'01:12','CHECKED_IN',false),
	(current_date,'641700005','417230101','417',123456789,'09:30:00',null,'01:12','CHECKED_IN',false);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
	(current_date,'641700007','417230101','417',123456789,null,null,'01:12',null,false),
    (current_date,'641700008','417230101','417',123456789,null,null,'01:12',null,false);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
    (current_date,'641800001','418230101','418',123456789,'09:30:00',null,'01:12','CHECKED_IN',false),
    (current_date,'641800002','418230102','418',123456789,'09:30:00',null,'01:12','CHECKED_IN',false),
    (current_date,'641800003','418230103','418',123456789,'09:30:00',null,'01:12','CHECKED_IN',false);

