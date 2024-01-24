DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;

DELETE FROM JUROR_DIGITAL.POOL_MEMBER_EXT;


INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '400', '2022-05-03', 5, 5, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,
optic_reference)
VALUES ('123456789', null,'LNAME','FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '12345678');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('400', '123456789', '415220502', true, '2022-05-03', 2);

INSERT INTO JUROR_DIGITAL.POOL_MEMBER_EXT (OWNER,PART_NO,POOL_NO,OPTIC_REFERENCE) VALUES ('400','123456789','415220502','12345678');
