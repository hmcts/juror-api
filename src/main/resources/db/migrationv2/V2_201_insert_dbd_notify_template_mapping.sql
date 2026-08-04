-- CONFIRMATION SERVICE ENGLISH

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (100001, '8a71632e-34a2-421e-9f17-100a419be7ba', 'date of letter', NULL, NULL, 'JUROR_DATE_OF_LETTER'),
    (100001, '8a71632e-34a2-421e-9f17-100a419be7ba', 'juror number', NULL, NULL, 'JUROR_NUMBER'),
    (100001, '8a71632e-34a2-421e-9f17-100a419be7ba', 'salutation logic', NULL, NULL, 'JUROR_SALUTATION_LOGIC'),
    (100001, '8a71632e-34a2-421e-9f17-100a419be7ba', 'court', NULL, NULL, 'COURT_LOC_COURT_NAME'),
    (100001, '8a71632e-34a2-421e-9f17-100a419be7ba', 'date of attendance', NULL, NULL, 'POOL_RETURN_DATE'),
    (100001, '8a71632e-34a2-421e-9f17-100a419be7ba', 'time of attendance', NULL, NULL, 'POOL_RETURN_TIME');

-- DEFERRAL GRANTED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (100001, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'date of letter', NULL, NULL, 'JUROR_DATE_OF_LETTER'),
    (100001, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'juror number', NULL, NULL, 'JUROR_NUMBER'),
    (100001, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'salutation logic', NULL, NULL, 'JUROR_SALUTATION_LOGIC'),
    (100001, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'deferral date', NULL, NULL, 'JUROR_DEFERRAL_DATE'),
    (100001, 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6', 'deferral time', NULL, NULL, 'JUROR_DEFERRAL_TIME');

-- DEFERRAL DENIED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (100001, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'date of letter', NULL, NULL, 'JUROR_DATE_OF_LETTER'),
    (100001, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'juror number', NULL, NULL, 'JUROR_NUMBER'),
    (100001, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'salutation logic', NULL, NULL, 'JUROR_SALUTATION_LOGIC'),
    (100001, '84066c25-e120-4a9c-a5e4-cdcc4c3c321d', 'prefixed name of court', NULL, NULL,
     'JUROR_PREFIXED_NAME_OF_COURT');

-- EXCUSAL GRANTED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (100001, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'date of letter', NULL, NULL, 'JUROR_DATE_OF_LETTER'),
    (100001, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'juror number', NULL, NULL, 'JUROR_NUMBER'),
    (100001, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'salutation logic', NULL, NULL, 'JUROR_SALUTATION_LOGIC'),
    (100001, 'e1f7f814-8321-45b2-8652-c61ad3d909ed', 'prefixed name of court', NULL, NULL,
     'JUROR_PREFIXED_NAME_OF_COURT');

-- EXCUSAL DENIED

INSERT INTO juror_mod.notify_template_field
(id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (100001, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'date of letter', NULL, NULL, 'JUROR_DATE_OF_LETTER'),
    (100001, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'juror number', NULL, NULL, 'JUROR_NUMBER'),
    (100001, 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74', 'salutation logic', NULL, NULL, 'JUROR_SALUTATION_LOGIC');