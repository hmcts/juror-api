-- Dedicated teardown for LaNotificationController tests only
-- Delete in order respecting foreign key constraints

DELETE FROM juror_er.reminder_history WHERE la_code IN ('001', '002', '003', '004', '005');
DELETE FROM juror_er."user" WHERE la_code IN ('001', '002', '003', '004', '005');
DELETE FROM juror_er.local_authority WHERE la_code IN ('001', '002', '003', '004', '005');
DELETE FROM juror_er.deadline WHERE id = 1;
DELETE FROM juror_mod.app_setting WHERE setting = 'NOTIFY_ER_REMINDER';
