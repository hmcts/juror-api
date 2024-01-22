-- juror
INSERT INTO JUROR.POOL (part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip, h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no, on_call, completion_flag, read_only, contact_preference, reg_spc, ret_date, status) VALUES (644892530, 'JANE', 'CASTILLO', 'jcastillo0@ed.gov', 'DR', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', '44(703)209-6993', '44(109)549-5625', 'Y', '400', '448', '44(145)525-2390', 'N', 21112, 555, 'N', 'N', 'N', 0, 'N' , CURRENT_DATE, 1);

-- enable court
INSERT INTO JUROR_DIGITAL.COURT_WHITELIST (LOC_CODE) VALUES ('448');

-- juror response
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (JUROR_NUMBER,DATE_RECEIVED,TITLE,FIRST_NAME,LAST_NAME,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ADDRESS6,ZIP,PROCESSING_STATUS,DATE_OF_BIRTH,PHONE_NUMBER,ALT_PHONE_NUMBER,EMAIL,RESIDENCY,RESIDENCY_DETAIL,MENTAL_HEALTH_ACT,MENTAL_HEALTH_ACT_DETAILS,BAIL,BAIL_DETAILS,CONVICTIONS,CONVICTIONS_DETAILS,DEFERRAL_REASON,DEFERRAL_DATE,SPECIAL_NEEDS_ARRANGEMENTS,EXCUSAL_REASON, PROCESSING_COMPLETE) VALUES (644892530, CURRENT_DATE,'DR','JANE','CASTILLO','4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom',null,null,'AB3 9RY','AWAITING_CONTACT',TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'),'44(703)209-6993','44(703)209-6993', 'jane@castillo.com','0',null,'0',null,'0',null,'0',null,null,null,null,null, 'N');
