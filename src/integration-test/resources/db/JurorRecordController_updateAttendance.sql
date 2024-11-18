-- create a pool record owned by 415

--add jurors
INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	  ('641600096','21111','CASTILLO','JANE','1984-07-24 00:00:00','4 Knutson Trail','Scotland','AB3 9RY','N',NULL,NULL,0,NULL),
	  ('121212121','21112','CASTILLO','JANE','1984-07-24 00:00:00','4 Knutson Trail','Scotland','AB3 9RY','N',NULL,NULL,0,NULL),
	  ('131313131','21113','CASTILLO','JANE','1984-07-24 00:00:00','4 Knutson Trail','Scotland','AB3 9RY','N',NULL,NULL,0,NULL);

--insert pool
INSERT INTO juror_mod.pool(pool_no, owner, return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('416220901', '415', '2022-05-03', 5, 5, 'CRO', '416', 'N');

--insert juror pool
INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status, on_call)
VALUES ('415', '641600096', '416220901', true, 2, true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status, on_call, next_date) VALUES
    ('415', '121212121', '416220901', true, 2, false, '2023-11-03 00:00:00' ),
    ('415', '131313131', '416220901', true, 2, false, '2023-11-03 00:00:00' );
