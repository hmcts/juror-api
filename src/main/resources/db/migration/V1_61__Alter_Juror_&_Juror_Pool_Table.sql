alter table juror_mod.juror
add travel_time float NULL,
add mileage int NULL,
add amount_spent float NULL,
add financial_loss float NULL;

alter table juror_mod.juror_pool
DROP COLUMN travel_time,
DROP COLUMN mileage,
DROP COLUMN amt_spent,
DROP COLUMN financial_loss;

