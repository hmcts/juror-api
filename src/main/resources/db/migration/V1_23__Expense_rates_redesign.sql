CREATE TABLE juror_mod.expense_rates
(
    id                                            bigserial     not null,
    rate_per_mile_car_0_passengers                numeric(8, 5) NULL,
    rate_per_mile_car_1_passengers                numeric(8, 5) NULL,
    rate_per_mile_car_2_or_more_passengers        numeric(8, 5) NULL,
    rate_per_mile_motorcycle_0_passengers         numeric(8, 5) NULL,
    rate_per_mile_motorcycle_1_or_more_passengers numeric(8, 5) NULL,
    rate_per_mile_bike                            numeric(8, 5) NULL,
    limit_financial_loss_half_day                 numeric(8, 5) NULL,
    limit_financial_loss_full_day                 numeric(8, 5) NULL,
    limit_financial_loss_half_day_long_trial      numeric(8, 5) NULL,
    limit_financial_loss_full_day_long_trial      numeric(8, 5) NULL,
    rate_subsistence_standard                     numeric(8, 5) NULL,
    rate_subsistence_long_day                     numeric(8, 5) NULL,
    rates_effective_from                          date          NULL,
    CONSTRAINT expense_rates_pkey PRIMARY KEY (id)
);

ALTER table juror_mod.court_location
    ADD COLUMN taxi_soft_limit numeric(8, 5) NULL,
    DROP column rate_per_mile_car_0_passengers,
    DROP column rate_per_mile_car_1_passengers,
    DROP column rate_per_mile_car_2_or_more_passengers,
    DROP column rate_per_mile_motorcycle_0_passengers,
    DROP column rate_per_mile_motorcycle_1_or_more_passengers,
    DROP column rate_per_mile_bike,
    DROP column limit_financial_loss_half_day,
    DROP column limit_financial_loss_full_day,
    DROP column limit_financial_loss_half_day_long_trial,
    DROP column limit_financial_loss_full_day_long_trial,
    DROP column rate_subsistence_standard,
    DROP column rate_subsistence_long_day,
    DROP column rates_effective_from;

ALTER table juror_mod.court_location_audit
    ADD COLUMN taxi_soft_limit numeric(8, 5) NULL,
    DROP column rate_per_mile_car_0_passengers,
    DROP column rate_per_mile_car_1_passengers,
    DROP column rate_per_mile_car_2_or_more_passengers,
    DROP column rate_per_mile_motorcycle_0_passengers,
    DROP column rate_per_mile_motorcycle_1_or_more_passengers,
    DROP column rate_per_mile_bike,
    DROP column limit_financial_loss_half_day,
    DROP column limit_financial_loss_full_day,
    DROP column limit_financial_loss_half_day_long_trial,
    DROP column limit_financial_loss_full_day_long_trial,
    DROP column rate_subsistence_standard,
    DROP column rate_subsistence_long_day,
    DROP column rates_effective_from;

ALTER table juror_mod.appearance
    ADD COLUMN expense_rates_id int8 null;
ALTER table juror_mod.appearance
    ADD CONSTRAINT appearance_expense_rate_fk foreign key (expense_rates_id) references juror_mod.expense_rates (id);

ALTER table juror_mod.appearance_audit
    ADD COLUMN expense_rates_id int8 null;
ALTER table juror_mod.appearance_audit
    ADD CONSTRAINT appearance_audit_expense_rate_fk foreign key (expense_rates_id) references juror_mod.expense_rates
        (id);

DROP table juror_mod.expense_rate;