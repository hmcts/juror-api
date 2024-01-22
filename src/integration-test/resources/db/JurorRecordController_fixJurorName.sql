DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.rev_info;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;


INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415230701', '400', '2023-07-04', 5, 5, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded)
VALUES ('111111111', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true);
INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded)
VALUES ('222222222', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('415', '111111111', '415230701', true, 2);
INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('400', '222222222', '415230701', true, 2);

INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp) VALUES
(1, EXTRACT (EPOCH FROM current_date)),
(2, EXTRACT (EPOCH FROM current_date));

INSERT INTO juror_mod.juror_audit
(revision, juror_number, rev_type, title, first_name, last_name, dob, address_line_1, address_line_4, postcode) VALUES
(1, '111111111', 1, NULL, 'FNAMEONE', 'LNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN'),
(2, '222222222', 1, NULL, 'FNAMEONE', 'LNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN');
