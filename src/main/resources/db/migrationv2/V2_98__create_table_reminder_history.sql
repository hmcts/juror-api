-- Create sequence for reminder_history id
CREATE SEQUENCE IF NOT EXISTS juror_er.reminder_history_id_seq
INCREMENT BY 1
MINVALUE 1
START 1;

-- Create reminder_history table
CREATE TABLE IF NOT EXISTS juror_er.reminder_history(
id BIGINT PRIMARY KEY DEFAULT nextval ('juror_er.reminder_history_id_seq'),
la_code VARCHAR(3) NOT NULL REFERENCES juror_er.local_authority(la_code),
sent_by VARCHAR(30) NOT NULL,
sent_to VARCHAR(200) NOT NULL,
time_sent TIMESTAMP(6) NOT NULL
);
