
INSERT INTO juror_mod.juror (juror_number, last_name, first_name, address_line_1, address_line_4, postcode, responded, excusal_code)
VALUES
('641600090', 'LNAMEFOURX', 'FNAMEFOUR', '4 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, 'D'),
('641600091', 'LNAMEFIVE', 'FNAMEFIVEX', '5 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, 'D'),
('641600092', 'LNAMESIX', 'FNAMESIX', '66 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, 'D'),
('641600093', 'LNAMENOMATCH', 'FNAMENOMATCH', '6 STREET NAME', 'ANYTOWN', 'CH99 2AN', true, 'D');

INSERT INTO juror_mod.excluded_voters (firstname,lastname,address_line1,postcode) VALUES
('FNAMEFOURX','LNAMEFOUR','4 STREET NAME','CH1 2AN'),
('FNAMEFIVE','LNAMEFIVEX','5 STREET NAME','CH1 2AN'),
('FNAMESEVEN','LNAMESEVEN','10 STREET NAME','CH1 2AN'),
('FNAMENOMATCHX','LNAMENOMATCH','7 STREET NAME','CH99 2AN');
