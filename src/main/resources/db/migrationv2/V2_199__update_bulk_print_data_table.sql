-- JS-1059 Digital by default updates to allow for email notifications to be sent to jurors.

ALTER TABLE juror_mod.bulk_print_data
  ADD COLUMN communication_channel varchar(10) NOT NULL DEFAULT 'LETTER',
    ADD COLUMN notify_template_name varchar(40) NULL,
    ADD COLUMN email_status varchar(20) null;

ALTER TABLE juror_mod.bulk_print_data
  ADD CONSTRAINT bulk_print_data_communication_channel_val
    CHECK (communication_channel IN ('LETTER', 'EMAIL'));

ALTER TABLE juror_mod.bulk_print_data
  ADD CONSTRAINT bulk_print_data_email_status_val
    CHECK (
      email_status IS NULL
        OR email_status IN ('PENDING', 'SENT')
      );

ALTER TABLE juror_mod.bulk_print_data
  ADD CONSTRAINT bulk_print_data_notify_template_fk
    FOREIGN KEY (notify_template_name)
      REFERENCES juror_mod.notify_template_mapping(template_name);

