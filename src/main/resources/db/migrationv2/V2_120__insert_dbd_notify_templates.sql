INSERT INTO juror_mod.notify_template_mapping
(template_id, template_name, notify_name, form_type, notification_type, "version")
VALUES
    ('8a71632e-34a2-421e-9f17-100a419be7ba', 'DBD_CONFIRM_ENG', 'DBD_Confirmation of Service (English)', '5224A', 1, 0),
    ('7093b2b4-2f71-478b-a179-92321a7f77a7', 'DBD_CONFIRM_CY', 'DBD_Confirmation of Service (Welsh)', '5224AC', 1, 0),
    ('b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'DBD_DEF_GRANTED_ENG', 'DBD_Deferral granted (English)', '5229A', 1, 0),
    ('534954fb-bb1b-4054-96a5-a196161d62e4', 'DBD_DEF_GRANTED_CY', 'DBD_Deferral granted (Welsh)', '5229AC', 1, 0),
    ('1827315e-9557-4985-ba53-395cb18f76dd', 'DBD_DEF_DENIED_ENG', 'DBD_Deferral denied (English)', '5226A', 1, 0),
    ('84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'DBD_DEF_DENIED_CY', 'DBD_Deferral denied (Welsh)', '5226AC', 1, 0),
    ('e1f7f814-8321-45b2-8652-c61ad3d909ed', 'DBD_EXC_GRANTED_ENG',  'DBD_Excusal granted (English)', '5225', 1, 0),
    ('9ee6488c-4ef2-4939-bb4d-a1d1a29264b7', 'DBD_EXC_GRANTED_CY',  'DBD_Excusal granted (Welsh)', '5225C', 1, 0),
    ('5d902f77-7505-487b-add3-b2f4ef0ed87e', 'DBD_EXC_DENIED_ENG', 'DBD_Deferral denied (English)', '5226', 1, 0),
    ('c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'DBD_EXC_DENIED_CY', 'DBD_Deferral denied (Welsh)', '5226C', 1, 0),
    ('bc749cd4-106d-4189-93c7-cac898bc2316', 'DBD_POSTPONE_ENG', 'DBD_Postponement (English)', '5229', 1, 0),
    ('7093999e-dfae-4165-955e-dc96ccba89f5', 'DBD_POSTPONE_CY', 'DBD_Postponement (Welsh)', '5229C', 1, 0),
    ('82b30231-1980-454a-b812-0e7953f7cccf', 'DBD_WITHDRAWAL_ENG', 'DBD_Disqualified (English)', '5224', 1, 0),
    ('55ed91ab-76c6-4ff7-908a-f2fe3c9d5824', 'DBD_WITHDRAWAL_CY', 'DBD_Disqualified (Welsh)', '5224C', 1, 0);

-- CONFIRMATION SERVICE ENGLISH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (340, '8a71632e-34a2-421e-9f17-100a419be7ba', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (341, '8a71632e-34a2-421e-9f17-100a419be7ba', 'COURTNAME', NULL, NULL, 'COURT_LOC_COURT_NAME'),
    (342, '8a71632e-34a2-421e-9f17-100a419be7ba', 'SERVICESTARTDATE', NULL, NULL, 'JUROR_POOL_NEXT_DATE'),
    (343, '8a71632e-34a2-421e-9f17-100a419be7ba', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (344, '8a71632e-34a2-421e-9f17-100a419be7ba', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (345, '8a71632e-34a2-421e-9f17-100a419be7ba', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (346, '8a71632e-34a2-421e-9f17-100a419be7ba', 'EMAILADDRESS', NULL, NULL, 'JUROR_EMAIL');

-- DEFERRAL GRANTED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (347, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (348, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'COURTNAME', NULL, NULL, 'COURT_LOC_COURT_NAME'),
    (349, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (350, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (351, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'SERVICESTARTDATE', NULL, NULL, 'JUROR_POOL_DEF_DATE'),
    (352, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (353, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'EMAILADDRESS', NULL, NULL, 'JUROR_EMAIL');

-- DEFERRAL DENIED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (354, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (355, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'COURTNAME', NULL, NULL,
     'COURT_LOC_COURT_NAME'),
    (356, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (357, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (358, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'SERVICESTARTDATE', NULL, NULL, 'JUROR_POOL_NEXT_DATE'),
    (359, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (360, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'EMAILADDRESS', NULL, NULL, 'JUROR_EMAIL');

-- EXCUSAL GRANTED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (361, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (362, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (363, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (364, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'EMAILADDRESS', NULL, NULL, 'JUROR_EMAIL'),
    (365, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'LOCATIONCODE', NULL, NULL,
     'JUROR_POOL_LOC_CODE');

-- EXCUSAL DENIED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (366, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (367, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'COURTNAME', NULL, NULL,
     'COURT_LOC_COURT_NAME'),
    (368, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (369, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (370, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'SERVICESTARTDATE', NULL, NULL, 'JUROR_POOL_NEXT_DATE'),
    (371, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (372, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'EMAILADDRESS', NULL, NULL, 'JUROR_EMAIL');

-- POSTPONEMENT

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (373, 'bc749cd4-106d-4189-93c7-cac898bc2316', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (374, 'bc749cd4-106d-4189-93c7-cac898bc2316', 'COURTNAME', NULL, NULL,
     'COURT_LOC_COURT_NAME'),
    (375, 'bc749cd4-106d-4189-93c7-cac898bc2316', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (376, 'bc749cd4-106d-4189-93c7-cac898bc2316', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (378, 'bc749cd4-106d-4189-93c7-cac898bc2316', 'SERVICESTARTDATE', NULL, NULL, 'JUROR_POOL_NEXT_DATE'),
    (379, 'bc749cd4-106d-4189-93c7-cac898bc2316', 'SERVICESTARTTIME', NULL, NULL, 'POOL_ATTEND_TIME'),
    (380, 'bc749cd4-106d-4189-93c7-cac898bc2316', 'EMAILADDRESS', NULL, NULL, 'JUROR_EMAIL');

-- WITHDRAWAL

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (381, '82b30231-1980-454a-b812-0e7953f7cccf', 'JURORNUMBER', NULL, NULL, 'JUROR_NUMBER'),
    (382, '82b30231-1980-454a-b812-0e7953f7cccf', 'FIRSTNAME', NULL, NULL, 'JUROR_FIRST_NAME'),
    (383, '82b30231-1980-454a-b812-0e7953f7cccf', 'LASTNAME', NULL, NULL, 'JUROR_LAST_NAME'),
    (384, '82b30231-1980-454a-b812-0e7953f7cccf', 'EMAILADDRESS', NULL, NULL, 'JUROR_EMAIL');
