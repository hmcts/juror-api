delete from juror_mod.contact_log;
delete from juror_mod.juror_audit;
delete from juror_mod.juror_history;
DELETE FROM juror_mod.rev_info;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

delete from juror_digital.juror_response;

-- Pool 55555555
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE) VALUES
	 ('448', '55555555', '2023-10-25 00:00:00', 4, 4, 'CRO','448', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

-- enable court
INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('644892530','21112','CASTILLO','JANE','1984-07-24 00:00:00','4 Knutson Trail','Scotland','AB3 9RY','N',NULL,NULL,0,NULL);
INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,ret_date,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','644892530','55555555','2023-10-25 00:00:00',NULL,1,'Y',NULL,'448','N');

-- response 644892530 (LAST_NAME changed)
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, SPECIAL_NEEDS_ARRANGEMENTS, EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION) VALUES ('644892530' , CURRENT_DATE, 'DR', 'JANE', 'DOE', '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', 'TODO', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '07032096993', '01095495625', 'jcastillo0@ed.gov', 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, NULL, NULL, NULL, NULL, 'N', 2);
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT (ID, JUROR_NUMBER, CJS_EMPLOYER, CJS_EMPLOYER_DETAILS) VALUES (1, '644892530', 'Police', 'I am a police special.');
INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS (ID, JUROR_NUMBER, SPEC_NEED, SPEC_NEED_DETAIL) VALUES (1, '644892530', 'V', 'Photosensitive epilepsy');

-- redundant amend entry 644892530
INSERT INTO juror_mod.rev_info
(revision_number, revision_timestamp) VALUES
(1, EXTRACT (EPOCH FROM current_date)),
(2, EXTRACT (EPOCH FROM current_date));

INSERT INTO juror_mod.juror_audit
(revision, juror_number, rev_type, first_name, last_name, dob, address_line_1, address_line_4, postcode) VALUES
(1, '644892530', 1, 'JANE', 'CASTILLO', null, '543 STREET NAME', 'ANYTOWN', 'CH1 2AN'),
(2, '644892530', 2, 'JANE', 'CASTILLO', '1984-07-24', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN');

-- redundant history entry 644892530
INSERT INTO juror_mod.juror_history
(juror_number, date_created, history_code, user_id, other_information) VALUES
('644892530', CURRENT_DATE, 'RSUM', 'TESTSQL', 'Summoned');

-- team leader with login enabled
INSERT INTO JUROR.PASSWORD (OWNER, LOGIN, PASSWORD, LAST_USED, USER_LEVEL, ARAMIS_AUTH_CODE, ARAMIS_MAX_AUTH, PASSWORD_CHANGED_DATE, LOGIN_ENABLED_YN) VALUES('400', 'testlogin', '5baa61e4c9b93f3f' , CURRENT_DATE - 3, 1, 123456789, 12345678.12 , CURRENT_DATE - 3, 'Y');
INSERT INTO JUROR_DIGITAL.STAFF (ACTIVE, LOGIN, NAME, RANK, TEAM_ID, VERSION, COURT_1, COURT_2, COURT_3, COURT_4, COURT_5, COURT_6, COURT_7, COURT_8, COURT_9, COURT_10) VALUES (1, 'testlogin', 'Test Login', 1, 1, 0, '120', '121', '122', '123', '124', '125', '126', '127', '128', '129');