-- Create table expense_rates_public

CREATE TABLE juror_mod.expense_rates_public (
    expense_type varchar(80) NOT NULL,
    rate float4 NULL,
    CONSTRAINT expense_rates_public_pkey PRIMARY KEY (expense_type)
    );
INSERT INTO juror_mod.expense_rates_public (expense_type,rate) VALUES
  ('TRAVEL_BICYCLE_PER_MILE','0.096'),
  ('TRAVEL_MOTORCYCLE_PER_MILE','0.314'),
  ('TRAVEL_CAR_PER_MILE','0.314'),
  ('SUBSISTENCE_PER_DAY','5.71'),
  ('EARNING_TEN_DAYS_FOUR_HRS_MORE','64.95'),
  ('EARNING_TEN_DAYS_FOUR_HRS_LESS','32.47');
