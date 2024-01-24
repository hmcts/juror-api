-- staff
INSERT INTO juror_mod.users (username, name, level, active, team_id, version, owner)
VALUES ('JPOWERS','Joanna Powers', 0, true, 1, 0,'446'),
('TSANCHEZ','Todd Sanchez', 0, true, 2, 0,'446'),
('GBECK','Grant Beck', 0, false, 3, 0,'446'),
('RPRICE','Roxanne Price', 1, true, 1, 0,'446'),
('PBREWER','Preston Brewer', 1, true, 2, 0,'626'),
('ACOPELAND','Amelia Copeland', 1, true, 3, 0,'400');

-- Unassigned urgent juror
INSERT INTO JUROR.POOL (
  part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip,
  h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no,
  on_call, completion_flag, read_only, contact_preference, reg_spc, ret_date, next_date, status) VALUES (
  '123251234', 'Gypsey', 'Hoola', 'jhoola@ed.gov', 'Mr', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'),
             '27 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB21 3RY',
  '44(703)209-6991', '44(109)549-5621', 'Y', 400, 446, '44(145)525-2391', 'N', 21112, 555,
  'N', 'N', 'N', 0, 'N', current_date, current_date + 60, 1);

INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (JUROR_NUMBER, DATE_RECEIVED) VALUES (123251234, current_date);

UPDATE JUROR_DIGITAL.JUROR_RESPONSE SET
  TITLE = 'Mr',
  FIRST_NAME = 'Gypsey',
  LAST_NAME = 'Hoola',
  ADDRESS = '27 Knutson Trail', ADDRESS2 = 'Scotland', ADDRESS3 = 'Aberdeen', ADDRESS4 = 'United Kingdom', ZIP = 'AB21 3RY',
  PROCESSING_STATUS = 'TODO',
  DATE_OF_BIRTH = TO_DATE('1984-07-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
  PHONE_NUMBER = '44(703)209-6991',
  ALT_PHONE_NUMBER = '44(145)525-2391',
  EMAIL = 'jhoola@ed.gov',
  RESIDENCY='Y',
  URGENT='Y',
  SUPER_URGENT='N'

WHERE JUROR_NUMBER = '123251234';

-- Unassigned super urgent juror
INSERT INTO JUROR.POOL (
  part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip,
  h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no,
  on_call, completion_flag, read_only, contact_preference, reg_spc, ret_date, next_date, status) VALUES (
  '209092530', 'Jane', 'Castillo', 'jcastillo0@ed.gov', 'Dr', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'),
             '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB21 3RY',
  '44(703)209-6993', '44(109)549-5625', 'Y', 400, 446, '44(145)525-2390', 'N', 21112, 555,
  'N', 'N', 'N', 0, 'N', current_date, current_date + 60, 1);


INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (JUROR_NUMBER, DATE_RECEIVED) VALUES ('209092530', current_date);

UPDATE JUROR_DIGITAL.JUROR_RESPONSE SET
  TITLE = 'Dr',
  FIRST_NAME = 'Jane',
  LAST_NAME = 'Castillo',
  ADDRESS = '4 Knutson Trail', ADDRESS2 = 'Scotland', ADDRESS3 = 'Aberdeen', ADDRESS4 = 'United Kingdom', ZIP = 'AB21 3RY',
  PROCESSING_STATUS = 'TODO',
  DATE_OF_BIRTH = TO_DATE('1984-07-24 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
  PHONE_NUMBER = '44(703)209-6993',
  ALT_PHONE_NUMBER = '44(145)525-2390',
  EMAIL = 'jcastillo0@ed.gov',
  RESIDENCY='Y',
  URGENT='N',
  SUPER_URGENT='Y'

WHERE JUROR_NUMBER = '209092530';
