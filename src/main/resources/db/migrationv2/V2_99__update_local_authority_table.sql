
ALTER TABLE juror_er.local_authority ADD COLUMN email_request_status VARCHAR(20);
ALTER TABLE juror_er.local_authority ADD COLUMN email_request_sent TIMESTAMP;

ALTER TABLE juror_er.local_authority_audit ADD COLUMN email_request_status VARCHAR(20);
ALTER TABLE juror_er.local_authority_audit ADD COLUMN email_request_sent TIMESTAMP;
