-- Clear previous Pool History
delete from juror_mod.pool_history;

-- Clear previous Participant History
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;

-- Create juror records associated with juror records
delete from juror_mod.contact_log;
delete from juror_mod.pool_comments;

delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

-- Clear Confirm Letter

-- Pool 415220401 requested 2 jurors for 2023-05-30, 4 already supplied (2 surplus) - active with the bureau
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220401', CURRENT_DATE+10, 2, 2, 'CRO', '415', 'N', CURRENT_DATE);

-- Pool 415220502 requested 4 jurors for 2023-06-01, 2 already supplied (2 needed) - active with the bureau
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220502' , CURRENT_DATE+10, 4, 4, 'CRO', '415', 'N', CURRENT_DATE+1);

-- Pool 415220503 requested 4 jurors for 2023-06-12, none currently supplied (4 needed) - active with the bureau
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220503' , CURRENT_DATE+10, 4, 4, 'CRO', '415', 'N', CURRENT_DATE+1);

-- Pool 415220504 requested 1 jurors for 2023-06-12, 1 already supplied (0 needed) - active with the court
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415220504', CURRENT_DATE+12, 1, 1, 'CRO', '415', 'N', CURRENT_DATE+1);

-- Pool 415220505 requested 5 jurors for 2023-06-19, none currently supplied (5 needed) - active with the court
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415220505', CURRENT_DATE+12, 5, 5, 'CRO', '415', 'N', CURRENT_DATE+1);

-- Pool 416220502 requested 3 jurors for 2023-06-01, 1 already supplied (2 needed) - active with the bureau
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '416220502', CURRENT_DATE+10, 3, 3, 'CRO', '416', 'N', CURRENT_DATE+1);

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '417220404', CURRENT_DATE+10, 3, 3, 'CRO', '417', 'N', CURRENT_DATE+1);

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '419220404', CURRENT_DATE+10, 3, 3, 'CRO', '419', 'N', CURRENT_DATE+1);

-- Pool 416220503 requested 4 jurors for 2023-06-12, 1 currently supplied (3 needed) - active with the bureau
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '416220503' , CURRENT_DATE+10, 4, 4, 'CRO', '416', 'N', CURRENT_DATE+1);

-- 416220504 requested 1 jurors for 2023-06-12, 1 already supplied (0 needed) - active with the court
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('416', '416220504', CURRENT_DATE+12, 1, 1, 'CRO', '416', 'N', CURRENT_DATE+1);

-- Pool 416220505 requested 4 jurors for 2023-06-12, 0 currently supplied (4 needed) - active with the bureau
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '416220505' , CURRENT_DATE+12, 4 ,4, 'CRO', '416', 'N', CURRENT_DATE+1);

-- Create POOL records

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,
                             notes,
optic_reference)
VALUES 
('555555551', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, 'These are some notes for 555555551', null),
('555555552', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '', null),
('555555553', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, '12345678'),
('555555554', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null),
('555555555', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null),
('555555556', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null),
('555555557', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null),
('555555558', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null),
('555555559', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null),
('555555560', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null),
('555555561', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES
('400', '555555551', '416220502', false, null, 8),
('400', '555555551', '415220401', true, CURRENT_DATE, 2),
('400', '555555552', '415220401', true, CURRENT_DATE, 2),
('400', '555555553', '415220401', true, CURRENT_DATE, 2),
('400', '555555554', '415220401', true, CURRENT_DATE, 2),
('400', '555555555', '415220502', true, CURRENT_DATE, 2),
('400', '555555556', '415220502', true, CURRENT_DATE, 2),
('400', '555555557', '419220404', true, CURRENT_DATE, 2),
('400', '555555558', '416220503', true, CURRENT_DATE, 2),
('417', '555555559', '417220404', true, CURRENT_DATE, 2),
('400', '555555560', '416220502', true, CURRENT_DATE, 2),
('400', '555555561', '416220502', false, null, 8),
('400', '555555561', '416220503', true, CURRENT_DATE, 2);
