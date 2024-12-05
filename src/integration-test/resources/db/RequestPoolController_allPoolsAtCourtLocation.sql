
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

-- a future pool
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('418', '418230104', CURRENT_DATE + 3, 10, 10, 'CRO', '418', 'N');

INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('419', '419230101', CURRENT_DATE - 10, 10, 10, 'CRO', '419', 'N');

-- POOL MEMBERS

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, completion_date) VALUES
('641800001', 'PERSON1', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800002', 'PERSON2', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800003', 'PERSON3', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800004', 'PERSON4', 'TEST4', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800005', 'PERSON5', 'TEST5', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641800006', 'PERSON6', 'TEST6', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641900001', 'PERSON1', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status, on_call) VALUES
('418', '641800001', '418230101', true, '2023-10-25', 2, false),
('418', '641800002', '418230102', true, '2023-10-23', 2, false),
('418', '641800003', '418230103', true, '2023-10-23', 2, false),
('418', '641800004', '418230102', true, '2023-10-23', 3, false),
('418', '641800005', '418230103', true, '2023-10-23', 4, false),
('418', '641800006', '418230104', true, CURRENT_DATE + 3, 2, false),
('419', '641900001', '419230101', true, '2023-10-23', 2, true);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance) VALUES
    (current_date,'641800001','418230101','418',123456789,'09:30:00',null,'01:12','CHECKED_IN',false),
    (current_date,'641800002','418230102','418',123456789,'09:30:00',null,'01:12','CHECKED_IN',false),
    (current_date,'641800003','418230103','418',123456789,'09:30:00',null,'01:12','CHECKED_IN',false);

