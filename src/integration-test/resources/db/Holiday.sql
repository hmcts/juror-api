DELETE FROM juror_mod.holiday;

INSERT INTO juror_mod.holiday
(loc_code, holiday, description, "public")
VALUES('415', '2023-09-28' , 'TEST', false);

INSERT INTO juror_mod.holiday
(holiday, description, "public")
VALUES('2023-09-28', 'TEST', true);

INSERT INTO juror_mod.holiday
(loc_code, holiday, description, "public")
VALUES('415', '2023-09-20' , 'TEST', false);

INSERT INTO juror_mod.holiday
(holiday, description, "public")
VALUES('2023-09-29', 'TEST PUBLIC HOLIDAY', true);