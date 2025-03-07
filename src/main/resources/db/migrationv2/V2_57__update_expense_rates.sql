
alter table juror_mod.expense_rates
add column limit_financial_loss_half_day_extra_long_trial numeric(8, 5) null,
add column limit_financial_loss_full_day_extra_long_trial numeric(8, 5) null;

update juror_mod.expense_rates
set limit_financial_loss_half_day_extra_long_trial = 114.3,
    limit_financial_loss_full_day_extra_long_trial = 228.6;

alter table juror_mod.appearance
 drop constraint attendance_type_val;

alter table juror_mod.appearance
 add constraint attendance_type_val check (((attendance_type)::text = ANY (ARRAY[('FULL_DAY'::character varying)::text, ('HALF_DAY'::character varying)::text, ('FULL_DAY_LONG_TRIAL'::character varying)::text, ('HALF_DAY_LONG_TRIAL'::character varying)::text, ('FULL_DAY_EXTRA_LONG_TRIAL'::character varying)::text, ('HALF_DAY_EXTRA_LONG_TRIAL'::character varying)::text, ('ABSENT'::character varying)::text, ('NON_ATTENDANCE'::character varying)::text, ('NON_ATTENDANCE_LONG_TRIAL'::character varying)::text, ('NON_ATT_EXTRA_LONG_TRIAL'::character varying)::text])));
