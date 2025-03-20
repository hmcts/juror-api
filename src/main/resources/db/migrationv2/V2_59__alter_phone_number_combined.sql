
ALTER TABLE juror_mod.juror
DROP COLUMN phone_number_combined ;


ALTER TABLE juror_mod.juror ADD COLUMN phone_number_combined VARCHAR(20) GENERATED always AS (COALESCE(h_phone, m_phone, w_phone)) STORED;
