-- RENAME juror_mod.app_settings to juror_mod app_setting
-- Insert Script into  juror_mod.app_setting


 ALTER TABLE juror_mod.app_settings RENAME TO app_setting;

-- INSERTING into juro_mod.app_setting

Insert into juror_mod.app_setting(setting,value)values ('WELSH_LANGUAGE_ENABLED','TRUE');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_STRAIGHT_THROUGH_INEL','daefcd8f-f03d-4259-ad0a-ac51f918185e');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_STRAIGHT_THROUGH_ADJ_INEL','e93dfaf1-9571-4c14-9b79-df917a9446b1');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_DEFERRAL_INEL','100f736f-cada-4beb-9a72-9ba6771df885');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_DEFERRAL_ADJ_INEL','cca9c936-dc7d-4a6b-95da-bede6d3b49df');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_EXCUSAL_INEL','48a8b747-eedc-4413-8ad8-8d96ffc5fda0');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_EXCUSAL_ADJ_INEL','96fc7021-58ec-4db8-be49-25f8756761aa');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_STRAIGHT_THROUGH_INEL','ac80a86b-194f-4aea-93ac-82c4b2080caf');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_STRAIGHT_THROUGH_ADJ_INEL','9efff046-1224-4f78-9ca4-d6077bb73583');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_DEFERRAL_INEL','9b7638e3-a234-4be9-994d-6ddcd394b991');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_DEFERRAL_ADJ_INEL','75647717-53f4-4e1f-bf2e-1f34eb64e68e');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_EXCUSAL_INEL','99f484f1-6032-4b5d-b8f4-16684726d6d3');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_EXCUSAL_ADJ_INEL','36db6216-0b6e-4c6b-b811-efeeb7268fab');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_STRAIGHT_THROUGH','aea4140b-2e2f-423b-8146-cd9615bfbc9e');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_STRAIGHT_THROUGH_INEL','a7228ccd-66ab-49d3-9f9e-1e1edbff32db');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ','df080a47-03d4-4427-808e-069a68d120ac');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_STRAIGHT_THROUGH_ADJ_INEL','b40de30f-1e36-4b1c-b3de-618f1f949c2a');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_DEFERRAL','14de6f3f-691c-443b-9a1d-af096ac4a794');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_DEFERRAL_INEL','62d2c570-497c-408c-a324-a90e7f759008');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_DEFERRAL_ADJ','087a65b6-c7df-4f8b-9a26-af8e7f4ffd09');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_DEFERRAL_ADJ_INEL','e1bbc66b-e496-4aa4-979b-37fb0444a890');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_EXCUSAL','2525dc38-af71-4a9d-b526-90b80d12007b');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_EXCUSAL_INEL','684dac69-5ebf-4673-bf4f-09a771d81f2e');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_EXCUSAL_ADJ','fdebed18-f7e1-4e0a-8d69-8175903d01f0');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_EXCUSAL_ADJ_INEL','b2e55e45-ff04-4368-9482-d14cc7ec47a9');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_1ST_DISQUALIFICATION_AGE','9d3d3e72-7db6-4c8a-9a9b-86e9771887f3');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_STRAIGHT_THROUGH','591f6e20-bfb8-44d0-92ad-b6b8b8889f49');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_STRAIGHT_THROUGH_INEL','b86a0862-6baf-4624-ad04-3c6a4dbff46c');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ','608ea9ac-4544-4479-940c-db4118bc4e89');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_STRAIGHT_THROUGH_ADJ_INEL','a5633187-c49f-4cb6-9da6-557e81c5d8a2');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_DEFERRAL','41cbe4fd-320f-454f-8d4a-2580fed362ca');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_DEFERRAL_INEL','a8d427b0-e872-466c-a620-7c85b4406b33');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_DEFERRAL_ADJ','b62d1f86-0afb-41f7-88e1-0182d7ae654f');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_DEFERRAL_ADJ_INEL','562b2f80-535a-454d-a255-a0528ed474b3');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_EXCUSAL','8c40826d-0a96-4ec7-8742-e9d62d9f09a5');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_EXCUSAL_INEL','e17799f4-c55a-46c0-beb9-d9394384bdd6');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_EXCUSAL_ADJ','13f9c9b2-e560-46f3-a1b1-44def42911f2');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_EXCUSAL_ADJ_INEL','83749019-a5c9-4304-a56d-bb5fe589d16e');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_EXCUSAL_DECEASED','939775ea-8534-4cf8-829e-3e2278a13316');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_CY_3RD_DISQUALIFICATION_AGE','79d34300-018b-45fc-bcfe-dd333e453fb0');
Insert into juror_mod.app_setting(setting,value)values ('SEND_EMAIL_OR_SMS','1');
Insert into juror_mod.app_setting(setting,value)values ('WELSH_TRANSLATION','Eich Gwasanaeth Rheithgor');
Insert into juror_mod.app_setting(setting,value)values ('SMART_SURVEY_SUMMONS_RESPONSE_SURVEY_ID','811887');
Insert into juror_mod.app_setting(setting,value)values ('SMART_SURVEY_SUMMONS_RESPONSE_DAYS','30');
Insert into juror_mod.app_setting(setting,value)values ('SMART_SURVEY_SUMMONS_RESPONSE_EXPORT_NAME','Daily Export');
Insert into juror_mod.app_setting(setting,value)values ('STRAIGHT_THROUGH_ACCEPTANCE_DISABLED','FALSE');
Insert into juror_mod.app_setting(setting,value)values ('STRAIGHT_THROUGH_AGE_EXCUSAL_DISABLED','FALSE');
Insert into juror_mod.app_setting(setting,value)values ('STRAIGHT_THROUGH_DECEASED_EXCUSAL_DISABLED','FALSE');
Insert into juror_mod.app_setting(setting,value)values ('SEARCH_RESULT_LIMIT_BUREAU_OFFICER','1000');
Insert into juror_mod.app_setting(setting,value)values ('SEARCH_RESULT_LIMIT_TEAM_LEADER','1000');
Insert into juror_mod.app_setting(setting,value)values ('AUTO_ASSIGNMENT_DEFAULT_CAPACITY','60');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_STRAIGHT_THROUGH','ec33ab68-b917-4f25-918e-50d3291edef6');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_STRAIGHT_THROUGH_ADJ','06881856-024c-4002-935f-23124cfdb5fa');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_DEFERRAL','771dda00-baf7-40f6-abe4-546ff1ec4872');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_DEFERRAL_ADJ','b6860c22-2d02-4b50-8fcf-b22eea54f922');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_EXCUSAL','f751c8f2-8ff4-4b77-adbb-a380b7eb344f');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_EXCUSAL_ADJ','33b62562-fdc6-4261-9139-4893fd8e1609');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_DISQUALIFICATION_AGE','414e3ee5-ebf3-423f-ad96-7ee91edc65c5');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_1ST_DISQUALIFICATION','daefcd8f-f03d-4259-ad0a-ac51f918185e');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_STRAIGHT_THROUGH','1701b1b1-1b7f-4a7c-b320-41e731480d6f');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_STRAIGHT_THROUGH_ADJ','d3b6ae23-c0d5-42f5-a6fb-bf85cedf7fad');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_DEFERRAL','fd2cd7dd-415b-4607-84a4-add28f563b59');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_DEFERRAL_ADJ','4f57c87b-1f4f-4f7d-9373-c9652cd8dfe6');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_EXCUSAL','de377ed7-8667-4895-9179-d476748d0da1');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_EXCUSAL_ADJ','c5ac3483-3ca2-4974-b010-f696e96e02e0');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_EXCUSAL_DECEASED','95568223-a642-408d-8dcc-a46eb2e6b14e');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_DISQUALIFICATION_AGE','393ae41c-be57-4fd9-9449-c5372ca1653c');
Insert into juror_mod.app_setting(setting,value)values ('NOTIFY_3RD_DISQUALIFICATION','ac80a86b-194f-4aea-93ac-82c4b2080caf');


Insert into juror_mod.app_setting(setting,value)values ('PCQ_SERVICE_URL','https://equality-and-diversity.platform.hmcts.net');
Insert into juror_mod.app_setting(setting,value)values ('PCQ_SERVICE_RETURN_URL','reply-jury-summons.service.gov.uk/steps/confirm-information');
Insert into juror_mod.app_setting(setting,value)values ('PCQ_SERVICE_ENABLED','TRUE');
Insert into juror_mod.app_setting(setting,value)values ('TOTAL_NUMBER_SUMMONSES_SENT','389603');
Insert into juror_mod.app_setting(setting,value)values ('TOTAL_NUMBER_ONLINE_REPLIES','176209');
Insert into juror_mod.app_setting(setting,value)values ('BUK_PRINT_DATA_BACKFILL_DAYS','3');
