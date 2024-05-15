-- added ordering as it reduces the cost overall.
DROP INDEX juror_response_staff_login_idx;
CREATE INDEX juror_response_staff_login_idx ON juror_mod.juror_response (staff_login ASC);
CREATE INDEX appearance_attendance_date_idx ON juror_mod.appearance USING btree (attendance_date, loc_code);
CREATE INDEX juror_pool_owner_juror_number_idx on juror_mod.juror_pool ("owner", juror_number);
CREATE INDEX pool_loc_code_idx ON juror_mod.pool (loc_code,pool_no);
CREATE INDEX pool_owner_idx ON juror_mod.pool ("owner",pool_no);
