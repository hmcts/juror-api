ALTER TABLE juror_mod.contact_log
ADD CONSTRAINT juror_number_fk FOREIGN KEY (juror_number) REFERENCES juror_mod.juror,
ADD CONSTRAINT t_contact_fk FOREIGN KEY (enquiry_type) REFERENCES juror_mod.t_contact (enquiry_code);
