-- Setup for LaEmailAddressController tests

-- Setup local authorities (sorted: Blackburn, Broxtowe, Eastleigh, Harrogate, West Oxfordshire)
INSERT INTO juror_er.local_authority (la_code, la_name, is_active, upload_status, notes, inactive_reason, updated_by, last_updated) VALUES
    ('001', 'West Oxfordshire', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('002', 'Broxtowe', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('003', 'Eastleigh', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('004', 'Blackburn', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('005', 'Harrogate', false, 'NOT_UPLOADED', NULL, 'Inactive LA', NULL, NULL);

-- Setup users (emails) for local authorities
INSERT INTO juror_er."user" (username, la_code, active, last_logged_in) VALUES
    ('user1@la001.gov.uk', '001', true, NULL),
    ('user2@la001.gov.uk', '001', true, NULL),     -- Multiple users for LA 001
    ('user1@la002.gov.uk', '002', true, NULL),     -- Single user for LA 002
    -- LA 003 has no users
    ('inactive@la004.gov.uk', '004', false, NULL), -- Inactive user for LA 004
    ('user1@la005.gov.uk', '005', true, NULL);     -- User for inactive LA 005
