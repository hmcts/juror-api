-- drop views dependent on expense columns in appearance table
drop view juror_mod.juror_expense_totals;
drop view juror_mod.juror_expense_subtotals;

-- drop multiple columns for loss of earnings
alter table juror_mod.appearance
    drop column loss_half_day_due;

alter table juror_mod.appearance
    drop column loss_half_day_paid;

alter table juror_mod.appearance
    drop column loss_full_day_due;

alter table juror_mod.appearance
    drop column loss_full_day_paid;

alter table juror_mod.appearance
    drop column loss_half_day_long_service_due;

alter table juror_mod.appearance
    drop column loss_half_day_long_service_paid;

alter table juror_mod.appearance
    drop column loss_full_day_long_service_due;

alter table juror_mod.appearance
    drop column loss_full_day_long_service_paid;

-- drop multiple columns for subsistence
alter table juror_mod.appearance
    drop column subs_half_day_due;

alter table juror_mod.appearance
    drop column subs_half_day_paid;

alter table juror_mod.appearance
    drop column subs_full_day_due;

alter table juror_mod.appearance
    drop column subs_full_day_paid;

alter table juror_mod.appearance
    drop column subs_long_day_due;

alter table juror_mod.appearance
    drop column subs_long_day_paid;

alter table juror_mod.appearance
    drop column subs_overnight_due;

alter table juror_mod.appearance
    drop column subs_overnight_paid;

-- add new generic columns for loss of earnings
alter table juror_mod.appearance
    add column loss_of_earnings_due numeric(8, 2);

alter table juror_mod.appearance
    add column loss_of_earnings_paid numeric(8, 2);

-- add new generic columns for subsistence
alter table juror_mod.appearance
    add column subsistence_due numeric(8, 2);

alter table juror_mod.appearance
    add column subsistence_paid numeric(8, 2);

-- add new column for attendance type with check constraint (restricting to enum values)
alter table juror_mod.appearance
    add column attendance_type varchar(25)
        CONSTRAINT attendance_type_val CHECK (attendance_type IN
                                              ('FULL_DAY', 'HALF_DAY', 'FULL_DAY_LONG_TRIAL', 'HALF_DAY_LONG_TRIAL',
                                               'ABSENT', 'NON_ATTENDANCE'));

-- recreate views dependent on expense columns in appearance table
create or replace view juror_mod.juror_expense_subtotals as
select a.juror_number,
       a.pool_number,
       j.first_name,
       j.last_name,
       a.loc_code,
       -- travel expenses
       sum(coalesce(public_transport_total_due, 0))  as public_transport_total_due_total,
       sum(coalesce(public_transport_total_paid, 0)) as public_transport_total_paid_total,
       sum(coalesce(hired_vehicle_total_due, 0))     as hired_vehicle_total_due_total,
       sum(coalesce(hired_vehicle_total_paid, 0))    as hired_vehicle_total_paid_total,
       sum(coalesce(motorcycle_total_due, 0))        as motorcycle_total_due_total,
       sum(coalesce(motorcycle_total_paid, 0))       as motorcycle_total_paid_total,
       sum(coalesce(car_total_due, 0))               as car_total_due_total,
       sum(coalesce(car_total_paid, 0))              as car_total_paid_total,
       sum(coalesce(pedal_cycle_total_due, 0))       as pedal_cycle_total_due_total,
       sum(coalesce(pedal_cycle_total_paid, 0))      as pedal_cycle_total_paid_total,
       sum(coalesce(parking_total_due, 0))           as parking_total_due_total,
       sum(coalesce(parking_total_paid, 0))          as parking_total_paid_total,
       -- financial loss - loss of earnings
       sum(coalesce(loss_of_earnings_due, 0))        as loss_of_earnings_due_total,
       sum(coalesce(loss_of_earnings_paid, 0))       as loss_of_earnings_paid_total,
       -- financial loss - childcare expenses
       sum(coalesce(childcare_total_due, 0))         as childcare_total_due_total,
       sum(coalesce(childcare_total_paid, 0))        as childcare_total_paid_total,
       -- financial loss - misc expenses
       sum(coalesce(misc_total_due, 0))              as misc_total_due_total,
       sum(coalesce(misc_total_paid, 0))             as misc_total_paid_total,
       -- subsistence
       sum(coalesce(subsistence_due, 0))             as subsistence_due_total,
       sum(coalesce(subsistence_paid, 0))            as subsistence_paid_total,
       -- deductions
       sum(coalesce(smart_card_spend, 0))            as smart_card_spend_total
from juror_mod.appearance a
         inner join juror_mod.juror j
                    on j.juror_number = a.juror_number
group by a.juror_number, a.pool_number, j.first_name, j.last_name, a.loc_code;


create or replace view juror_mod.juror_expense_totals as
select juror_number,
       pool_number,
       first_name,
       last_name,
       loc_code,
       -- travel expenses
       public_transport_total_due_total + hired_vehicle_total_due_total + motorcycle_total_due_total +
       car_total_due_total + pedal_cycle_total_due_total + parking_total_due_total        as travel_unapproved,
       public_transport_total_paid_total + hired_vehicle_total_paid_total + motorcycle_total_paid_total +
       car_total_paid_total + pedal_cycle_total_paid_total + parking_total_paid_total     as travel_approved,
       -- financial loss
       loss_of_earnings_due_total                                                         as financial_loss_unapproved,
       loss_of_earnings_paid_total                                                        as financial_loss_approved,
       -- subsistence
       subsistence_due_total                                                              as subsistence_unapproved,
       subsistence_paid_total                                                             as subsistence_approved,
       -- deductions
       smart_card_spend_total,
       -- totals
       public_transport_total_due_total + hired_vehicle_total_due_total + motorcycle_total_due_total +
       car_total_due_total + pedal_cycle_total_due_total + parking_total_due_total + childcare_total_due_total +
       misc_total_due_total + loss_of_earnings_due_total + subsistence_due_total          as total_unapproved,
       public_transport_total_paid_total + hired_vehicle_total_paid_total + motorcycle_total_paid_total +
       car_total_paid_total + pedal_cycle_total_paid_total + parking_total_paid_total + childcare_total_paid_total
           + misc_total_paid_total + loss_of_earnings_paid_total + subsistence_paid_total as total_approved
from juror_mod.juror_expense_subtotals;

CREATE TABLE juror_mod.financial_audit_details
(
    id                           bigint,
    submitted_on                 timestamp(6) NOT NULL,
    submitted_by                 varchar(20)  NOT NULL,
    approved_on                  timestamp(6) NULL,
    approved_by                  varchar(20)  NULL,
    juror_revision_when_approved bigint       NULL,

    CONSTRAINT financial_audit_details_pkey PRIMARY KEY (id),

    CONSTRAINT financial_audit_details_fk_approved_by FOREIGN KEY (approved_by)
        REFERENCES juror_mod.users (username),
    CONSTRAINT financial_audit_details_fk_submitted_by FOREIGN KEY (submitted_by)
        REFERENCES juror_mod.users (username),
    CONSTRAINT financial_audit_details_fk_revision_number FOREIGN KEY (juror_revision_when_approved)
        REFERENCES juror_mod.rev_info (revision_number)
);

CREATE TABLE juror_mod.appearance_audit
(
    revision                    bigint        NOT NULL,
    rev_type                    integer       NULL,

    attendance_date             date          NOT NULL,
    juror_number                varchar(9)    NOT NULL,
    loc_code                    varchar(3)    NOT NULL,
    time_in                     time          NULL,
    time_out                    time          NULL,
    pool_trial_no               varchar(16)   NULL,
    non_attendance              bool          NULL,
    no_show                     bool          NULL,
    mileage_due                 int4          NULL,
    mileage_paid                int4          NULL,
    misc_description            varchar(50)   NULL,
    pay_cash                    bool          NULL,
    last_updated_by             varchar(20)   NULL,
    created_by                  varchar(20)   NULL,
    public_transport_total_due  numeric(8, 2) NULL,
    public_transport_total_paid numeric(8, 2) NULL,
    hired_vehicle_total_due     numeric(8, 2) NULL,
    hired_vehicle_total_paid    numeric(8, 2) NULL,
    motorcycle_total_due        numeric(8, 2) NULL,
    motorcycle_total_paid       numeric(8, 2) NULL,
    car_total_due               numeric(8, 2) NULL,
    car_total_paid              numeric(8, 2) NULL,
    pedal_cycle_total_due       numeric(8, 2) NULL,
    pedal_cycle_total_paid      numeric(8, 2) NULL,
    childcare_total_due         numeric(8, 2) NULL,
    childcare_total_paid        numeric(8, 2) NULL,
    parking_total_due           numeric(8, 2) NULL,
    parking_total_paid          numeric(8, 2) NULL,
    misc_total_due              numeric(8, 2) NULL,
    misc_total_paid             numeric(8, 2) NULL,
    smart_card_due              numeric(8, 2) NULL,
    smart_card_paid             numeric(8, 2) NULL,
    travel_time                 numeric(4, 2) NULL,
    payment_approved_date       date          NULL,
    expense_submitted_date      date          NULL,
    is_draft_expense            bool          NULL,
    f_audit                     int8          NULL,
    sat_on_jury                 bool          NULL,
    pool_number                 varchar(9)    NULL,
    appearance_stage            varchar(25)   NULL,
    loss_of_earnings_due        numeric(8, 2) NULL,
    loss_of_earnings_paid       numeric(8, 2) NULL,
    subsistence_due             numeric(8, 2) NULL,
    subsistence_paid            numeric(8, 2) NULL,
    attendance_type             varchar(25)   NULL,
    CONSTRAINT appearance_audit_pkey PRIMARY KEY (revision, juror_number, attendance_date, loc_code),
    CONSTRAINT fk_revision_number FOREIGN KEY (revision) REFERENCES juror_mod.rev_info (revision_number),
    CONSTRAINT fk_f_audit FOREIGN KEY (f_audit) REFERENCES juror_mod.financial_audit_details (id)
);

ALTER TABLE juror_mod.appearance
    ADD COLUMN mileage_paid    int4,
    ADD COLUMN smart_card_paid numeric(8, 2);

ALTER TABLE juror_mod.appearance
    RENAME COLUMN mileage TO mileage_due;
ALTER TABLE juror_mod.appearance
    RENAME COLUMN smart_card_spend TO smart_card_due;