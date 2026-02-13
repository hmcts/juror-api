DELETE FROM juror_er.reminder_history;
DELETE FROM juror_er.file_uploads;
DELETE FROM juror_er."user";
DELETE FROM juror_er.local_authority;
DELETE FROM juror_er.deadline;
DELETE FROM juror_mod.app_setting WHERE setting = 'NOTIFY_ER_REMINDER';

-- Setup local authorities with email addresses
INSERT INTO juror_er.local_authority (la_code, la_name, email, is_active, upload_status, notes, inactive_reason, updated_by, last_updated) VALUES
    ('001', 'West Oxfordshire', 'la001@council.gov.uk', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('002', 'Broxtowe', 'la002@council.gov.uk', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('003', 'Eastleigh', 'la003@council.gov.uk', true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL),
    ('004', 'Blackburn', NULL, true, 'NOT_UPLOADED', NULL, NULL, NULL, NULL), -- No email
    ('005', 'Harrogate', 'la005@council.gov.uk', false, 'NOT_UPLOADED', NULL, 'Inactive LA', NULL, NULL); -- Inactive

-- Setup deadline
INSERT INTO juror_er.deadline (id, deadline_date, upload_start_date, updated_by, last_updated) VALUES
    (1, current_date + interval '6 weeks', current_date - interval '6 weeks', NULL, NULL);

-- Setup Notify template ID in app_setting
INSERT INTO juror_mod.app_setting (setting, value) VALUES
    ('NOTIFY_ER_REMINDER', 'test-template-id-12345');
