INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('416220901', '416', current_date - 20, 5, 5, 'CRO', '416', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded) values
('641600096', 'MR','LNAME1','FNAME1', '1988-01-01', '542 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600097', 'MR','LNAME2','FNAME2', '1988-01-02', '543 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600098', 'MR','LNAME3','FNAME3', '1988-01-03', '544 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600099', 'MR','LNAME4','FNAME4', '1988-01-04', '545 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600100', 'MR','LNAME5','FNAME5', '1988-01-01', '542 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600101', 'MR','LNAME6','FNAME6', '1988-01-02', '543 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600102', 'MR','LNAME7','FNAME7', '1988-01-03', '544 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600103', 'MR','LNAME8','FNAME8', '1988-01-04', '545 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600104', 'MR','LNAME9','FNAME9', '1988-01-04', '545 STREET NAME', 'Chichester', 'PO19 1SX', true),
('641600105', 'MR','LNAME10','FNAME10', '1988-01-04', '545 STREET NAME', 'Chichester', 'PO19 1SX', true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status) values
('416', '641600096', '416220901', true, 1),
('416', '641600097', '416220901', true, 2),
('416', '641600098', '416220901', true, 2),
('416', '641600099', '416220901', true, 3),
('416', '641600100', '416220901', false, 3),
('416', '641600101', '416220901', false, 6),
('416', '641600102', '416220901', false, 6),
('416', '641600103', '416220901', false, 6),
('416', '641600104', '416220901', false, 9),
('416', '641600105', '416220901', false, 9);
