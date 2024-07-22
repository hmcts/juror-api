DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;

INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('416220901', '416', '2022-05-03', 5, 5, 'CRO', '416', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,
summons_file, reasonable_adj_code, reasonable_adj_msg, optic_reference, welsh, response_entered)
VALUES ('641600090', null, 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, 'M', 'Reasonable adjustment test message', '12345678', true, true),
('641600091', null, 'LNAMEFIVEFOURONE', 'FNAMEFIVEFOURONE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
 null, 'M', 'Reasonable adjustment test message', '87654321', false, true),
('641600092', null, 'LNAMEFIVEFOURTWO', 'FNAMEFIVEFOURTWO', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
 null, 'M', 'Reasonable adjustment test message', null, null, true),
('641600093', null, 'LNAMEFIVEFOURTHREE', 'FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN',
true, 'Disq. on selection', null, null, false, null, null),
('641600094', null, 'LNAMEFIVEFOURFOUR', 'FNAMEFIVEFOURFOUR', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN',
true, null, null, null, null, null, null),
('641600095', null, 'LNAMEFIVEFOURFIVE', 'FNAMEFIVEFOURFIVE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN',
true, null, null, null, null, null, null);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('416', '641600090', '416220901', true, 3),
('416', '641600091', '416220901', true, 2),
('400', '641600092', '416220901', true, 1),
('416', '641600093', '416220901', true, 6),
('416', '641600094', '416220901', true, 2),
('416', '641600095', '416220901', true, 6);
