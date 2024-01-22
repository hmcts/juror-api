--Juror with missing Postcode
INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4,
postcode, responded) VALUES
      ('111111111', NULL, 'Smith', 'Liam random', '1987-10-03', '543 STREET NAME', 'ANYTOWN', 'M244BP',true),
      ('121212121', NULL, 'Smith', 'Liam random', '1987-10-03', '543 STREET NAME', 'ANYTOWN', '',true),
      ('333333333', NULL, 'Smith', 'Liam random', NULL, '543 STREET NAME', 'ANYTOWN', 'M244BP',true);