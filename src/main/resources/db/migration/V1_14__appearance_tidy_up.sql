ALTER TABLE juror_mod.appearance
    DROP COLUMN pay_attendance_type;
ALTER TABLE juror_mod.appearance_audit
    DROP COLUMN pay_attendance_type;

ALTER TABLE juror_mod.appearance
    DROP CONSTRAINT attendance_type_val;

ALTER TABLE juror_mod.appearance
    ADD CONSTRAINT attendance_type_val CHECK (((attendance_type)::text = ANY ((ARRAY [
        'FULL_DAY'::character varying,
        'HALF_DAY'::character varying,
        'FULL_DAY_LONG_TRIAL'::character varying,
        'HALF_DAY_LONG_TRIAL'::character varying,
        'ABSENT'::character varying,
        'NON_ATTENDANCE'::character varying,
        'NON_ATTENDANCE_LONG_TRIAL'::character varying
        ])::text[])));
