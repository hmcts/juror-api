-- alter column and app_setting update

ALTER TABLE juror_er.reminder_history
    ADD COLUMN notification_type varchar(20) NOT NULL DEFAULT 'REMINDER_EMAIL';



    INSERT INTO juror_mod.app_setting (setting, value) VALUES
    ('NOTIFY_ER_INITIAL' ,'5238501b-146a-462e-894c-e629db5a854b');
