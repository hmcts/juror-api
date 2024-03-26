

INSERT INTO juror_mod.users(owner, username,email, name, active, last_logged_in, version, team_id)
VALUES ('400', 'ksalazar','ksalazar@email.gov.uk', 'Kris Salazar', true, CURRENT_DATE-3, 0, 1);

INSERT INTO juror_mod.expense_rates_public values ('TRAVEL_BICYCLE_PER_MILE', 0.096);

INSERT INTO juror_mod.expense_rates_public values ('TRAVEL_MOTORCYCLE_PER_MILE', 0.314);

INSERT INTO juror_mod.expense_rates_public values ('TRAVEL_CAR_PER_MILE', 0.314);

INSERT INTO juror_mod.expense_rates_public values ('SUBSISTENCE_PER_DAY', 5.71);

INSERT INTO juror_mod.expense_rates_public values ('EARNING_TEN_DAYS_FOUR_HRS_MORE', 64.95);

INSERT INTO juror_mod.expense_rates_public values ('EARNING_TEN_DAYS_FOUR_HRS_LESS', 32.47);
