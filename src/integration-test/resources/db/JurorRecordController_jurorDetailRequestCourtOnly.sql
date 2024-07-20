DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;



INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('416220901', '416', '2022-05-03', 5, 5, 'CRO', '416', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,
h_phone, reasonable_adj_code, reasonable_adj_msg, optic_reference)
VALUES ('641600090', 'MR', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1989-03-31', '542 STREET NAME', 'Chichester',
'PO19 1SX', true, '0202 2342345', 'M', 'Reasonable adjustment test message', '18273645');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status, response_entered)
VALUES ('416', '641600090', '416220901', true, '2022-05-03', 2, true);
