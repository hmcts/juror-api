

INSERT INTO juror_mod.users(created_by,updated_by,username,email, name, active, last_logged_in, team_id)
VALUES ('ksalazar','ksalazar','ksalazar','ksalazar@email.gov.uk', 'Kris Salazar', true, CURRENT_DATE-3, 1);

insert into juror_mod.user_courts (username, loc_code)
values ('ksalazar', '400');

INSERT INTO juror_mod.expense_rates_public values ('TRAVEL_BICYCLE_PER_MILE', 0.096);

INSERT INTO juror_mod.expense_rates_public values ('TRAVEL_MOTORCYCLE_PER_MILE', 0.314);

INSERT INTO juror_mod.expense_rates_public values ('TRAVEL_CAR_PER_MILE', 0.314);

INSERT INTO juror_mod.expense_rates_public values ('SUBSISTENCE_PER_DAY', 5.71);

INSERT INTO juror_mod.expense_rates_public values ('EARNING_TEN_DAYS_FOUR_HRS_MORE', 64.95);

INSERT INTO juror_mod.expense_rates_public values ('EARNING_TEN_DAYS_FOUR_HRS_LESS', 32.47);
