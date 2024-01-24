DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded)
VALUES ('333333333', 'LNAMEFIVEFOURTWO', 'FNAMEFIVEFOURTWO', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN',
true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('415', '333333333', '415221001', true, '2022-10-03', 2);