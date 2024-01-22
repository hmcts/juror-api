-- #### 352004504 - First person response.
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (JUROR_NUMBER,DATE_RECEIVED,TITLE,FIRST_NAME,LAST_NAME,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ADDRESS6,ZIP,PROCESSING_STATUS,DATE_OF_BIRTH,PHONE_NUMBER,ALT_PHONE_NUMBER,EMAIL,RESIDENCY,RESIDENCY_DETAIL,MENTAL_HEALTH_ACT,MENTAL_HEALTH_ACT_DETAILS,BAIL,BAIL_DETAILS,CONVICTIONS,CONVICTIONS_DETAILS,DEFERRAL_REASON,DEFERRAL_DATE,SPECIAL_NEEDS_ARRANGEMENTS,EXCUSAL_REASON,PROCESSING_COMPLETE,VERSION,THIRDPARTY_FNAME,THIRDPARTY_LNAME,RELATIONSHIP,MAIN_PHONE,OTHER_PHONE,EMAIL_ADDRESS,THIRDPARTY_REASON,THIRDPARTY_OTHER_REASON,JUROR_PHONE_DETAILS,JUROR_EMAIL_DETAILS,STAFF_LOGIN,STAFF_ASSIGNMENT_DATE,URGENT,SUPER_URGENT,COMPLETED_AT) VALUES ('352004504',to_date('23-OCT-17','DD-MON-YY'),'Rev','Jose','Rivera','22177 Redwing Way','England','London','United Kingdom',NULL,NULL,'EC3M 2NY','TODO',to_date('08-AUG-95','DD-MON-YY'),'11111111111','00000000000','email@email.com','Y',NULL,'N',NULL,'N',NULL,'N',NULL,NULL,NULL,NULL,NULL,'N',0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Y','Y',NULL,NULL,'Y','N',NULL);

-- Juror CJS employment details
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT (JUROR_NUMBER,CJS_EMPLOYER,CJS_EMPLOYER_DETAILS,ID) values ('352004504','Police Force','These are details about it',0);
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT (JUROR_NUMBER,CJS_EMPLOYER,CJS_EMPLOYER_DETAILS,ID) values ('352004504','National Crime Agency','National Crime Agency',1);

INSERT INTO JUROR.POOL (OWNER,PART_NO,POOL_NO,POLL_NUMBER,TITLE,LNAME,FNAME,DOB,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ADDRESS6,ZIP,H_PHONE,W_PHONE,W_PH_LOCAL,TIMES_SEL,TRIAL_NO,JUROR_NO,REG_SPC,RET_DATE,DEF_DATE,RESPONDED,DATE_EXCUS,EXC_CODE,ACC_EXC,DATE_DISQ,DISQ_CODE,MILEAGE,LOCATION,USER_EDTQ,STATUS,NOTES,NO_ATTENDANCES,IS_ACTIVE,NO_DEF_POS,NO_ATTENDED,NO_FTA,NO_AWOL,POOL_SEQ,EDIT_TAG,POOL_TYPE,LOC_CODE,NEXT_DATE,ON_CALL,PERM_DISQUAL,PAY_COUNTY_EMP,PAY_EXPENSES,SPEC_NEED,SPEC_NEED_MSG,SMART_CARD,AMT_SPENT,COMPLETION_FLAG,COMPLETION_DATE,SORT_CODE,BANK_ACCT_NAME,BANK_ACCT_NO,BLDG_SOC_ROLL_NO,WAS_DEFERRED,ID_CHECKED,POSTPONE,WELSH,PAID_CASH,TRAVEL_TIME,SCAN_CODE,FINANCIAL_LOSS,POLICE_CHECK,LAST_UPDATE,READ_ONLY,SUMMONS_FILE,REMINDER_SENT,PHOENIX_DATE,PHOENIX_CHECKED,M_PHONE,H_EMAIL,CONTACT_PREFERENCE) VALUES ('400','352004504','222','76024','Rev','Rivera','Jose',to_date('25-MAY-87','DD-MON-YY'),'22177 Redwing Way','England','London','United Kingdom',NULL,NULL,'EC3M 2NY','44(406)759-6616','44(322)292-4490',NULL,NULL,NULL,NULL,'N',to_date('23-OCT-17','DD-MON-YY'),NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,'Y',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'443',to_date('13-NOV-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,to_date('23-OCT-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,'44(362)527-9947','jriverac@myspace.com',0);
INSERT INTO JUROR.POOL (OWNER,PART_NO,POOL_NO,POLL_NUMBER,TITLE,LNAME,FNAME,DOB,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ADDRESS6,ZIP,H_PHONE,W_PHONE,W_PH_LOCAL,TIMES_SEL,TRIAL_NO,JUROR_NO,REG_SPC,RET_DATE,DEF_DATE,RESPONDED,DATE_EXCUS,EXC_CODE,ACC_EXC,DATE_DISQ,DISQ_CODE,MILEAGE,LOCATION,USER_EDTQ,STATUS,NOTES,NO_ATTENDANCES,IS_ACTIVE,NO_DEF_POS,NO_ATTENDED,NO_FTA,NO_AWOL,POOL_SEQ,EDIT_TAG,POOL_TYPE,LOC_CODE,NEXT_DATE,ON_CALL,PERM_DISQUAL,PAY_COUNTY_EMP,PAY_EXPENSES,SPEC_NEED,SPEC_NEED_MSG,SMART_CARD,AMT_SPENT,COMPLETION_FLAG,COMPLETION_DATE,SORT_CODE,BANK_ACCT_NAME,BANK_ACCT_NO,BLDG_SOC_ROLL_NO,WAS_DEFERRED,ID_CHECKED,POSTPONE,WELSH,PAID_CASH,TRAVEL_TIME,SCAN_CODE,FINANCIAL_LOSS,POLICE_CHECK,LAST_UPDATE,READ_ONLY,SUMMONS_FILE,REMINDER_SENT,PHOENIX_DATE,PHOENIX_CHECKED,M_PHONE,H_EMAIL,CONTACT_PREFERENCE) VALUES ('400','209092530','222','76024','Dr','Castillo','Jane',to_date('24-JUL-84','DD-MON-YY'),'4 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,NULL,'AB39RY','44(703)209-6993','44(109)549-5625',NULL,NULL,NULL,NULL,'N',to_date('23-OCT-17','DD-MON-YY'),NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,'Y',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'443',to_date('13-NOV-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,to_date('23-OCT-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,'44(362)527-9947','jcastillo0@ed.gov',0);
INSERT INTO JUROR.POOL (OWNER,PART_NO,POOL_NO,POLL_NUMBER,TITLE,LNAME,FNAME,DOB,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ADDRESS6,ZIP,H_PHONE,W_PHONE,W_PH_LOCAL,TIMES_SEL,TRIAL_NO,JUROR_NO,REG_SPC,RET_DATE,DEF_DATE,RESPONDED,DATE_EXCUS,EXC_CODE,ACC_EXC,DATE_DISQ,DISQ_CODE,MILEAGE,LOCATION,USER_EDTQ,STATUS,NOTES,NO_ATTENDANCES,IS_ACTIVE,NO_DEF_POS,NO_ATTENDED,NO_FTA,NO_AWOL,POOL_SEQ,EDIT_TAG,POOL_TYPE,LOC_CODE,NEXT_DATE,ON_CALL,PERM_DISQUAL,PAY_COUNTY_EMP,PAY_EXPENSES,SPEC_NEED,SPEC_NEED_MSG,SMART_CARD,AMT_SPENT,COMPLETION_FLAG,COMPLETION_DATE,SORT_CODE,BANK_ACCT_NAME,BANK_ACCT_NO,BLDG_SOC_ROLL_NO,WAS_DEFERRED,ID_CHECKED,POSTPONE,WELSH,PAID_CASH,TRAVEL_TIME,SCAN_CODE,FINANCIAL_LOSS,POLICE_CHECK,LAST_UPDATE,READ_ONLY,SUMMONS_FILE,REMINDER_SENT,PHOENIX_DATE,PHOENIX_CHECKED,M_PHONE,H_EMAIL,CONTACT_PREFERENCE) VALUES ('400','122444503','222','76024','Mr','Wilson','Wade',to_date('25-MAY-87','DD-MON-YY'),'123 Fake Road','England','London','United Kingdom',NULL,NULL,'BC3M 2ND','44(406)759-6616','44(322)292-4490',NULL,NULL,NULL,NULL,'N',to_date('23-OCT-17','DD-MON-YY'),NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,'Y',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'443',to_date('23-NOV-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,to_date('23-OCT-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,'44(362)527-9947','captain_deadpool@gmail.com',0);
INSERT INTO JUROR.POOL (OWNER,PART_NO,POOL_NO,POLL_NUMBER,TITLE,LNAME,FNAME,DOB,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4,ADDRESS5,ADDRESS6,ZIP,H_PHONE,W_PHONE,W_PH_LOCAL,TIMES_SEL,TRIAL_NO,JUROR_NO,REG_SPC,RET_DATE,DEF_DATE,RESPONDED,DATE_EXCUS,EXC_CODE,ACC_EXC,DATE_DISQ,DISQ_CODE,MILEAGE,LOCATION,USER_EDTQ,STATUS,NOTES,NO_ATTENDANCES,IS_ACTIVE,NO_DEF_POS,NO_ATTENDED,NO_FTA,NO_AWOL,POOL_SEQ,EDIT_TAG,POOL_TYPE,LOC_CODE,NEXT_DATE,ON_CALL,PERM_DISQUAL,PAY_COUNTY_EMP,PAY_EXPENSES,SPEC_NEED,SPEC_NEED_MSG,SMART_CARD,AMT_SPENT,COMPLETION_FLAG,COMPLETION_DATE,SORT_CODE,BANK_ACCT_NAME,BANK_ACCT_NO,BLDG_SOC_ROLL_NO,WAS_DEFERRED,ID_CHECKED,POSTPONE,WELSH,PAID_CASH,TRAVEL_TIME,SCAN_CODE,FINANCIAL_LOSS,POLICE_CHECK,LAST_UPDATE,READ_ONLY,SUMMONS_FILE,REMINDER_SENT,PHOENIX_DATE,PHOENIX_CHECKED,M_PHONE,H_EMAIL,CONTACT_PREFERENCE) VALUES ('400','152004504','222','76024','Mr','Reynolds','Frank',to_date('25-MAY-87','DD-MON-YY'),'123 Fake Street','England','London','United Kingdom',NULL,NULL,'BC3M 2NY','44(406)759-6616','44(322)292-4490',NULL,NULL,NULL,NULL,'N',to_date('23-OCT-17','DD-MON-YY'),NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,'Y',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'443',to_date('23-NOV-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,to_date('23-OCT-17','DD-MON-YY'),'N',NULL,NULL,NULL,NULL,'44(362)527-9947','frankie_fast_hands@gmail.com',0);

-- staff
INSERT INTO juror_mod.users (owner, username, name, level, active, password,version,team_id)
VALUES ('448','BUREAUGUY1','Bureau Guy',1,true,'5BAA61E4C9B93F3F',0,1),
       ('448','BUREAULADY9','Bureau Lady',1,false,'5BAA61E4C9B93F3F',0,1);
