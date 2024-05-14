-- added ordering as it reduces the cost overall.
DROP INDEX juror_response_staff_login_idx;
CREATE INDEX juror_response_staff_login_idx ON juror_mod.juror_response (staff_login ASC);
CREATE INDEX appearance_attendance_date_idx ON juror_mod.appearance USING btree (attendance_date, loc_code);
CREATE INDEX juror_pool_juror_number_owner_idx on juror_mod.juror_pool (juror_number, owner);