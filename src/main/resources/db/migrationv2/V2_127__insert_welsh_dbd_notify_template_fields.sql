@ -0,0 +1,88 @@
-- CONFIRMATION SERVICE WELSH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (385, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (386, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'COURTADDRESS', NULL, NULL, 'COURT_LOC_ADDRESS'),
    (387, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'SERVICESTARTDATE', NULL, NULL, 'JUROR_POOL_NEXT_DATE'),
    (388, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (389, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (390, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (391, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'email address', NULL, NULL, 'JUROR_EMAIL');

-- DEFERRAL GRANTED WELSH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (392, '534954fb-bb1b-4054-96a5-a196161d62e4', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (393, '534954fb-bb1b-4054-96a5-a196161d62e4', 'COURTNAME', NULL, NULL, 'COURT_LOC_COURT_NAME'),
    (394, '534954fb-bb1b-4054-96a5-a196161d62e4', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (395, '534954fb-bb1b-4054-96a5-a196161d62e4', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (396, '534954fb-bb1b-4054-96a5-a196161d62e4', 'SERVICESTARTDATE', NULL, NULL,
     'JUROR_POOL_SERVICE_START_DATE'),
    (397, '534954fb-bb1b-4054-96a5-a196161d62e4', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (398, '534954fb-bb1b-4054-96a5-a196161d62e4', 'email address', NULL, NULL, 'JUROR_EMAIL');

-- DEFERRAL DENIED WELSH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (399, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (400, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'COURTNAME', NULL, NULL, 'COURT_LOC_COURT_NAME'),
    (401, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (402, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (403, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'SERVICESTARTDATE', NULL, NULL,
     'JUROR_POOL_SERVICE_START_DATE'),
    (404, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (405, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'email address', NULL, NULL, 'JUROR_EMAIL');

-- EXCUSAL GRANTED WELSH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (406, '9ee6488c-4ef2-4939-bb4d-a1d1a29264b7', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (407, '9ee6488c-4ef2-4939-bb4d-a1d1a29264b7', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (408, '9ee6488c-4ef2-4939-bb4d-a1d1a29264b7', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (409, '9ee6488c-4ef2-4939-bb4d-a1d1a29264b7', 'email address', NULL, NULL, 'JUROR_EMAIL'),
    (410, '9ee6488c-4ef2-4939-bb4d-a1d1a29264b7', 'LOCATIONCODE', NULL, NULL, 'JUROR_POOL_LOC_CODE');

-- EXCUSAL DENIED WELSH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (411, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (412, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'COURTNAME', NULL, NULL, 'COURT_LOC_COURT_NAME'),
    (413, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (414, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (415, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'SERVICESTARTDATE', NULL, NULL, 'JUROR_POOL_NEXT_DATE'),
    (416, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (417, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'email address', NULL, NULL, 'JUROR_OR_RESPONSE_EMAIL');

-- POSTPONEMENT WELSH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (418, '7093999e-dfae-4165-955e-dc96ccba89f5', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (419, '7093999e-dfae-4165-955e-dc96ccba89f5', 'COURTNAME', NULL, NULL, 'COURT_LOC_COURT_NAME'),
    (420, '7093999e-dfae-4165-955e-dc96ccba89f5', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (421, '7093999e-dfae-4165-955e-dc96ccba89f5', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (422, '7093999e-dfae-4165-955e-dc96ccba89f5', 'SERVICESTARTDATE', NULL, NULL,
     'JUROR_POOL_SERVICE_START_DATE'),
    (423, '7093999e-dfae-4165-955e-dc96ccba89f5', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (424, '7093999e-dfae-4165-955e-dc96ccba89f5', 'email address', NULL, NULL, 'JUROR_OR_RESPONSE_EMAIL');

-- WITHDRAWAL WELSH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (425, '55ed91ab-76c6-4ff7-908a-f2fe3c9d5824', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (426, '55ed91ab-76c6-4ff7-908a-f2fe3c9d5824', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (427, '55ed91ab-76c6-4ff7-908a-f2fe3c9d5824', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (428, '55ed91ab-76c6-4ff7-908a-f2fe3c9d5824', 'email address', NULL, NULL, 'JUROR_OR_RESPONSE_EMAIL');