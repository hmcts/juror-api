alter table juror_mod.juror
    add column phone_number_combined VARCHAR(20) generated always
    as (COALESCE(m_phone, h_phone, w_phone)) STORED;