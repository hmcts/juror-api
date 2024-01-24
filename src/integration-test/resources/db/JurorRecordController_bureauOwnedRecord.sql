DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;


INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '400', '2022-05-03', 5, 5, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, notes)
VALUES ('123456789', null,'LNAME','FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, 'SOME EXAMPLE NOTES');
INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, notes)
VALUES ('111111111', null,'LNAME','FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('400', '123456789', '415220502', true, 2);
INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('400', '111111111', '415220502', true, 2);

DELETE FROM juror_mod.contact_log;

INSERT INTO juror_mod.contact_log
(juror_number, user_id, notes, last_update, start_call, end_call, enquiry_type, repeat_enquiry)
VALUES('123456789', 'BUREAU_USER', 'Some general communication occurred', TIMESTAMP '2022-12-12 16:15:55.000000', TIMESTAMP '1990-07-25 10:12:14.000000', NULL, 'GE', NULL);
