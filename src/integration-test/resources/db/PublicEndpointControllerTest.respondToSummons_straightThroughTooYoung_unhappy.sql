-- juror (straight through elligible, basic path)
INSERT INTO JUROR.POOL (part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip, h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no, on_call, completion_flag, read_only, contact_preference, reg_spc, ret_date, status, next_date) VALUES ('644892530', 'JANE', 'CASTILLO', 'jcastillo0@ed.gov', 'DR', CURRENT_DATE - interval '204 month', '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB39RY', '44(703)209-6993', '44(109)549-5625', 'Y', '400', '448', '44(145)525-2390', 'N', 21112, 555, 'N', 'N', 'N', 0, 'N', CURRENT_DATE, 1 , CURRENT_DATE);

-- enable court
INSERT INTO JUROR_DIGITAL.COURT_WHITELIST (LOC_CODE) VALUES ('448');
