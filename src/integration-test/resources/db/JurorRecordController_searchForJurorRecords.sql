INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('416220901', '416', '2022-12-03', 5, 5, 'CRO', '416', 'N'),
('416220902', '416', '2022-12-03', 5, 5, 'CRO', '416', 'N'),
('415220901', '415', '2022-12-03', 5, 5, 'CRO', '415', 'N'),
('767220901', '415', '2022-12-03', 5, 5, 'CRO', '767', 'N'),
('767220902', '400', '2022-12-03', 5, 5, 'CRO', '767', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded)
VALUES ('641600091', 'MR','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641600090', 'MR','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500091', 'MR','Lnamenineone','Fnamenineone', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500092', 'MR','Lnameninetwo','Fnameninetwo', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500093', 'MR','Lnameninethree','Fnameninethree', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500094', 'MR','Lnameninefour','Fnameninefour', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500095', 'MR','Lnameninefive','Fnameninefive', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500096', 'MR','Lnameninesix','Fnameninesix', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500097', 'MR','Lnamenineseven','Fnamenineseven', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500098', 'MR','Lnamenineeight','Fnamenineeight', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500099', 'MR','Lnameninenine','Fnameninenine', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500100', 'MR','Lnamenineten','Fnamenineten', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true),
('641500101', 'MR','Lnamenineten','Fnamenineten', '1988-01-01', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('416', '641600091', '416220901', false, 7),
('416', '641600091', '416220902', true, 2),
('416', '641600090', '416220901', true, 2),
('415', '641500091', '415220901', true, 2),
('415', '641500092', '415220901', true, 2),
('415', '641500093', '415220901', true, 2),
('415', '641500094', '415220901', true, 2),
('415', '641500095', '415220901', true, 2),
('415', '641500096', '415220901', true, 2),
('415', '641500097', '415220901', true, 2),
('415', '641500098', '415220901', true, 2),
('415', '641500099', '415220901', true, 2),
('415', '641500100', '415220901', true, 2),
('400', '641500101', '767220902', true, 2);
