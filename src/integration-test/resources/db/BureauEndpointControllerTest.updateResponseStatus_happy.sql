-- juror 644892530
INSERT INTO JUROR.POOL (part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip, h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no, on_call, completion_flag, read_only, contact_preference, reg_spc, ret_date, next_date, status) VALUES ('644892530', 'JANE', 'CASTILLO', 'jcastillo0@ed.gov', 'DR', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', '07032096993', '01095495625', 'Y', '400', '448', '01455252390', 'N', 21112, 555, 'N', 'N', 'N', 0, 'N' , CURRENT_DATE, CURRENT_DATE + 35, 1);

-- enable court
INSERT INTO JUROR_DIGITAL.COURT_WHITELIST (LOC_CODE) VALUES ('448');

-- response 644892530 (LAST_NAME changed)
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, ADDRESS, ADDRESS2, ADDRESS3, ADDRESS4, ZIP, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, SPECIAL_NEEDS_ARRANGEMENTS, EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION) VALUES ('644892530' , CURRENT_DATE, 'DR', 'JANE', 'DOE', '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', 'TODO', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '07032096993', '01095495625', 'jcastillo0@ed.gov', 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, NULL, NULL, NULL, NULL, 'N', 2);
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT (ID, JUROR_NUMBER, CJS_EMPLOYER, CJS_EMPLOYER_DETAILS) VALUES (1, '644892530', 'Police', 'I am a police special.');
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS (ID, JUROR_NUMBER, SPEC_NEED, SPEC_NEED_DETAIL) VALUES (1, '644892530', 'V', 'Photosensitive epilepsy');

-- redundant amend entry 644892530
INSERT INTO JUROR.PART_AMENDMENTS(OWNER, PART_NO, EDIT_DATE, EDIT_USERID, TITLE, FNAME, LNAME, DOB, ADDRESS, ZIP) VALUES (400, '644892530', (SELECT CURRENT_DATE), 'TESTSQL', ' ', null, null, TO_DATE('1901-01-01 00:00:01', 'YYYY-MM-DD HH24:MI:SS'), null, null);

-- redundant history entry 644892530
INSERT INTO JUROR.PART_HIST(OWNER, PART_NO, DATE_PART, HISTORY_CODE, USER_ID, OTHER_INFORMATION, POOL_NO, LAST_UPDATE) VALUES (400, '644892530', (SELECT CURRENT_DATE), 'RSUM', 'TESTSQL', 'Summoned', '', (SELECT CURRENT_DATE));

-- team leader with login enabled


INSERT INTO juror_mod.users(owner, username, name, level, active, last_logged_in, version, team_id, password,
                            password_changed_date)
VALUES ('400', 'testlogin', 'Test Login', 1, true, CURRENT_DATE-3, 0, 1, '5baa61e4c9b93f3f', CURRENT_DATE-3);
--


