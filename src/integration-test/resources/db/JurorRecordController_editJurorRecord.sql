DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;

INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '400', '2022-05-03', 5, 5, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,
optic_reference)
VALUES ('123456789', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '11111111');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('400', '123456789', '415220502', true, 2);
