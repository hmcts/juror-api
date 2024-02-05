--Car Rate
ALTER TABLE juror_mod.court_location
    ADD COLUMN rate_per_mile_car_0_passengers         numeric(8, 5),
    ADD COLUMN rate_per_mile_car_1_passengers         numeric(8, 5),
    ADD COLUMN rate_per_mile_car_2_or_more_passengers numeric(8, 5);

--Motorcycle Rate
ALTER TABLE juror_mod.court_location
    ADD COLUMN rate_per_mile_motorcycle_0_passengers         numeric(8, 5),
    ADD COLUMN rate_per_mile_motorcycle_1_or_more_passengers numeric(8, 5);
--Bike Rate
ALTER TABLE juror_mod.court_location
    ADD COLUMN rate_per_mile_bike numeric(8, 2);

-- Financial loss
ALTER TABLE juror_mod.court_location
    ADD COLUMN limit_financial_loss_half_day            numeric(8, 5),
    ADD COLUMN limit_financial_loss_full_day            numeric(8, 5),
    ADD COLUMN limit_financial_loss_half_day_long_trial numeric(8, 5),
    ADD COLUMN limit_financial_loss_full_day_long_trial numeric(8, 5),
    ADD COLUMN public_transport_soft_limit              numeric(8, 5);

--Substance
ALTER TABLE juror_mod.court_location
    ADD COLUMN rate_substance_standard numeric(8, 5),
    ADD COLUMN rate_substance_long_day numeric(8, 5);

ALTER TABLE juror_mod.appearance
    ADD COLUMN pay_attendance_type varchar(25)
        CONSTRAINT pay_attendance_type_val CHECK (pay_attendance_type IN
                                                  ('FULL_DAY', 'HALF_DAY', 'FULL_DAY_LONG_TRIAL', 'HALF_DAY_LONG_TRIAL',
                                                   'NON_ATTENDANCE'));
ALTER TABLE juror_mod.appearance_audit
    ADD COLUMN pay_attendance_type varchar(25)
        CONSTRAINT pay_attendance_type_val CHECK (pay_attendance_type IN
                                                  ('FULL_DAY', 'HALF_DAY', 'FULL_DAY_LONG_TRIAL', 'HALF_DAY_LONG_TRIAL',
                                                   'NON_ATTENDANCE'));

ALTER TABLE juror_mod.appearance
    DROP COLUMN travel_time;
ALTER TABLE juror_mod.appearance_audit
    DROP COLUMN travel_time;

ALTER TABLE juror_mod.appearance
    ADD COLUMN travel_time time without time zone null;

ALTER TABLE juror_mod.appearance_audit
    ADD COLUMN travel_time time without time zone null;

ALTER TABLE juror_mod.appearance
    ADD COLUMN travel_jurors_taken_by_car        int,
    ADD COLUMN travel_by_car                     bool,
    ADD COLUMN travel_jurors_taken_by_motorcycle int,
    ADD COLUMN travel_by_motorcycle              bool,
    ADD COLUMN travel_by_bicycle                 bool,
    ADD COLUMN miles_traveled                    int,
    ADD COLUMN food_and_drink_claim_type         varchar(20)
        CONSTRAINT food_and_drink_claim_type_val CHECK (food_and_drink_claim_type IN
                                                        ('NONE', 'LESS_THAN_1O_HOURS', 'MORE_THAN_10_HOURS'))
;

ALTER TABLE juror_mod.appearance_audit
    ADD COLUMN travel_jurors_taken_by_car        int,
    ADD COLUMN travel_by_car                     bool,
    ADD COLUMN travel_jurors_taken_by_motorcycle int,
    ADD COLUMN travel_by_motorcycle              bool,
    ADD COLUMN travel_by_bicycle                 bool,
    ADD COLUMN miles_traveled                    int,
    ADD COLUMN food_and_drink_claim_type         varchar(20)
        CONSTRAINT food_and_drink_claim_type_val CHECK (food_and_drink_claim_type IN
                                                        ('NONE', 'LESS_THAN_1O_HOURS', 'MORE_THAN_10_HOURS'))
;

INSERT INTO juror_mod.app_settings(setting, value)
VALUES ('LONG_DAY_THRESHOLD', null);


-- Financial loss
ALTER TABLE juror_mod.court_location
    ADD COLUMN rates_effective_from date;

CREATE TABLE juror_mod.court_location_audit
(
    revision                                      bigint        NOT NULL,
    rev_type                                      integer       NULL,
    loc_code                                      varchar(3)    NOT NULL,
    rates_effective_from                          date          NULL,
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
    rate_substance_standard                       numeric(8, 5) NULL,
    rate_substance_long_day                       numeric(8, 5) NULL,
    public_transport_soft_limit                   numeric(8, 5) NULL,
    CONSTRAINT court_location_audit_pkey PRIMARY KEY (revision, loc_code),
    CONSTRAINT fk_revision_number FOREIGN KEY (revision) REFERENCES juror_mod.rev_info (revision_number)
);




