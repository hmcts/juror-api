--- data setup for the Postponement journey
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_pool;

-- Create POOL records associated with POOL recrods
DELETE FROM juror_mod.pool;

INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST) VALUES
-- Pool 415220401 requested 2 jurors, 4 already supplied (2 surplus) - active with the bureau
 ('400', '415220401', CURRENT_DATE, 4, 2, 'CRO', '415', 'N'),
-- Pool 415220503 requested 4 jurors, one currently supplied (3 needed) - active with the bureau
 ('400', '415220503', CURRENT_DATE + 20, 4, 3, 'CRO', '415', 'N'),
 ('415', '222222222', CURRENT_DATE + 20, 4, 3, 'CRO', '400', 'N');

-- Create juror records associated with Pool request records

DELETE FROM juror_mod.juror;

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded) VALUES
('555555551', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', TIMESTAMP '1990-07-25 00:00:00.000000', '540 STREET NAME',
 'ANYTOWN', 'CH1 2AN', true),
('555555552', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', TIMESTAMP '1990-07-25 00:00:00.000000', '540 STREET NAME',
'ANYTOWN', 'CH1 2AN', true),
('555555557', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', TIMESTAMP '1990-07-25 00:00:00.000000', '540 STREET NAME',
'ANYTOWN', 'CH1 2AN', true),
('555555558', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', TIMESTAMP '1990-07-25 00:00:00.000000', '540 STREET NAME',
'ANYTOWN', 'CH1 2AN', true),
('555555559', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', TIMESTAMP '1990-07-25 00:00:00.000000', '540 STREET NAME',
'ANYTOWN', 'CH1 2AN', true),
('555555560', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', TIMESTAMP '1990-07-25 00:00:00.000000', '540 STREET NAME',
'ANYTOWN', 'CH1 2AN', true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active) VALUES
('400', '555555551', '415220401', 2, true),
('400', '555555552', '415220401', 2, true),
('400', '555555557', '415220503', 2, true),
('400', '555555558', '415220401', 2, true),
('415', '555555559', '222222222', 2, true),
('415', '555555560', '222222222', 2, true);

-- add appearance values for cert of attendance values
INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,time_in,time_out,
travel_time,appearance_stage, loss_of_earnings_paid, childcare_total_paid, misc_total_paid, non_attendance, no_show,
attendance_type)
VALUES
	(current_date,'555555560','222222222','415','09:30:00',NULL,'01:12','CHECKED_IN', null, null, null, false, false, NULL);
