-- enable court
INSERT INTO JUROR_DIGITAL.COURT_WHITELIST (LOC_CODE) VALUES ('448');

-- juror
INSERT INTO JUROR.POOL (part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip, h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no, on_call, completion_flag, read_only, contact_preference, reg_spc, next_date, ret_date, status) VALUES ('644892530', 'JANE', 'CASTILLO', 'jcastillo0@ed.gov', 'DR', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', '44(703)209-6993', '44(109)549-5625', 'Y', '400', '448', '44(145)525-2390', 'N', 21112, 555, 'N', 'N', 'N', 0, 'N', juror_mod.next_day(( CURRENT_DATE-15),'MONDAY'), juror_mod.next_day(( CURRENT_DATE-15),'MONDAY'), 1);

