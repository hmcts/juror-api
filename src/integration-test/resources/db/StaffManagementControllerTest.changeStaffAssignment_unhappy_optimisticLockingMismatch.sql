--staff
INSERT INTO juror_mod.users (owner, username, name, level, active, password,team_id,version)
VALUES ('400','jmcbob','Joe McBob',0,true,'5BAA61E4C9B93F3F',1,0),
       ('400','smcbob','Sarah McBob',1,true,'5BAA61E4C9B93F3F',2,0);

-- juror 644892530
INSERT INTO JUROR.POOL (part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip, h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no, on_call, completion_flag, read_only, contact_preference, reg_spc, ret_date, status) VALUES (644892530, 'JANE', 'CASTILLO', 'jcastillo0@ed.gov', 'DR', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', '44(703)209-6993', '44(109)549-5625', 'Y', '400', '448', '44(145)525-2390', 'N', 21112, 555, 'N', 'N', 'N', 0, 'N' , CURRENT_DATE, 1);

-- enable court
INSERT INTO JUROR_DIGITAL.COURT_WHITELIST (LOC_CODE) VALUES ('448');

-- response 644892530 (LAST_NAME changed)
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, ADDRESS, ADDRESS2, ADDRESS3, ADDRESS4, ZIP, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, SPECIAL_NEEDS_ARRANGEMENTS, EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE) VALUES ('644892530' , CURRENT_DATE, 'DR', 'JANE', 'DOE', '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', 'TODO', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '44(703)209-6993', '44(109)549-5625', 'jcastillo0@ed.gov', 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, NULL, NULL, NULL, NULL, 'N', 2, 'jmcbob' , CURRENT_DATE);
