ALTER TABLE juror_mod.appearance

ADD COLUMN mileage int,
ADD COLUMN misc_description varchar(50),
ADD COLUMN pay_cash boolean,
ADD COLUMN last_updated_by varchar(20),
ADD COLUMN created_by varchar(20),

ADD COLUMN public_transport_total_due numeric(8, 2),
ADD COLUMN public_transport_total_paid numeric(8, 2),
ADD COLUMN hired_vehicle_total_due numeric(8, 2),
ADD COLUMN hired_vehicle_total_paid numeric(8, 2),
ADD COLUMN motorcycle_total_due numeric(8, 2),
ADD COLUMN motorcycle_total_paid numeric(8, 2),
ADD COLUMN car_total_due numeric(8, 2),
ADD COLUMN car_total_paid numeric(8, 2),
ADD COLUMN pedal_cycle_total_due numeric(8, 2),
ADD COLUMN pedal_cycle_total_paid numeric(8, 2),
ADD COLUMN childcare_total_due numeric(8, 2),
ADD COLUMN childcare_total_paid numeric(8, 2),
ADD COLUMN parking_total_due numeric(8, 2),
ADD COLUMN parking_total_paid numeric(8, 2),
ADD COLUMN misc_total_due numeric(8, 2),
ADD COLUMN misc_total_paid numeric(8, 2),

ADD COLUMN loss_half_day_due numeric(8, 2),
ADD COLUMN loss_half_day_paid numeric(8, 2),
ADD COLUMN loss_full_day_due numeric(8, 2),
ADD COLUMN loss_full_day_paid numeric(8, 2),
ADD COLUMN loss_half_day_long_service_due numeric(8, 2),
ADD COLUMN loss_half_day_long_service_paid numeric(8, 2),
ADD COLUMN loss_full_day_long_service_due numeric(8, 2),
ADD COLUMN loss_full_day_long_service_paid numeric(8, 2),
ADD COLUMN subs_half_day_due numeric(8, 2),
ADD COLUMN subs_half_day_paid numeric(8, 2),
ADD COLUMN subs_full_day_due numeric(8, 2),
ADD COLUMN subs_full_day_paid numeric(8, 2),
ADD COLUMN subs_long_day_due numeric(8, 2),
ADD COLUMN subs_long_day_paid numeric(8, 2),
ADD COLUMN subs_overnight_due numeric(8, 2),
ADD COLUMN subs_overnight_paid numeric(8, 2),

ADD COLUMN smart_card_spend numeric(8, 2),
ADD COLUMN travel_time numeric(4, 2),
ADD COLUMN payment_approved_date date,
ADD COLUMN expense_submitted_date date,
ADD COLUMN is_draft_expense boolean,
ADD COLUMN f_audit bigserial,
ADD COLUMN sat_on_jury boolean,
ADD COLUMN pool_number varchar(9),

ADD COLUMN appearance_stage varchar(25) CONSTRAINT appearance_stage_val CHECK (appearance_stage IN
('CHECKED_IN', 'CHECKED_OUT', 'APPEARANCE_CONFIRMED', 'EXPENSE_ENTERED', 'EXPENSE_AUTHORISED', 'EXPENSE_EDITED'));

ALTER TABLE juror_mod.appearance
-- replaced by appearance_stage
DROP COLUMN app_stage,
-- replaced by payment_approved_date
DROP COLUMN date_paid,
-- replace with a view summing the totals for each row in this table
DROP COLUMN amount,
-- no longer required in new schema design
DROP COLUMN audits,
-- replaced by sat_on_jury
DROP COLUMN court_emp,
-- replaced by f_audit (sequence generate value with big integer data type)
DROP COLUMN faudit;

ALTER TABLE juror_mod.appearance
RENAME COLUMN timein to time_in;
ALTER TABLE juror_mod.appearance
RENAME COLUMN timeout to time_out;
ALTER TABLE juror_mod.appearance
RENAME COLUMN att_date to attendance_date;
