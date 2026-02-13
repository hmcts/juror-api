DELETE FROM juror_er.reminder_history;
DELETE FROM juror_er."user";
DELETE FROM juror_er.local_authority;
DELETE FROM juror_er.deadline;
DELETE FROM juror_mod.app_setting WHERE setting = 'NOTIFY_ER_REMINDER';


INSERT INTO juror_er.local_authority (la_code, la_name, is_active, upload_status, notes, inactive_reason, updated_by, last_updated) VALUES
    ('001', 'West Oxfordshire', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('002', 'Broxtowe', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('003', 'Eastleigh', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('004', 'Blackburn', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('005', 'Harrogate', false, 'NOT_UPLOADED', NULL, 'Inactive LA', NULL, NULL);

-- Setup users (emails) for local authorities
INSERT INTO juror_er."user" (username, la_code, active, last_logged_in) VALUES
    ('user1@la001.gov.uk', '001', true, NULL),
    ('user2@la001.gov.uk', '001', true, NULL),  -- Multiple users for LA 001
    ('user1@la002.gov.uk', '002', true, NULL),
    ('user1@la003.gov.uk', '003', true, NULL),
    ('inactive@la004.gov.uk', '004', false, NULL),  -- Inactive user - won't receive email
    ('user1@la005.gov.uk', '005', true, NULL);  -- Active user but LA is inactive

-- Setup deadline
INSERT INTO juror_er.deadline (id, deadline_date, upload_start_date, updated_by, last_updated) VALUES
    (1, current_date + interval '6 weeks', current_date - interval '6 weeks', NULL, NULL);

-- Setup Notify template ID in app_setting
INSERT INTO juror_mod.app_setting (setting, value) VALUES
    ('NOTIFY_ER_REMINDER', 'test-template-id-12345');
