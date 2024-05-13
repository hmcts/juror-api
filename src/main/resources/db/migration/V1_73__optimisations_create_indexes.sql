-- added ordering as it reduces the cost overall.
DROP INDEX juror_response_staff_login_idx;
CREATE INDEX juror_response_staff_login_idx ON juror_mod.juror_response (staff_login ASC);
CREATE INDEX appearance_attendance_date_idx ON juror_mod.appearance USING btree (attendance_date, loc_code);
