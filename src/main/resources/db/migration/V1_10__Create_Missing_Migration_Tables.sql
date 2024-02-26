-- create the juror_mod.t_contact_preference table
drop table if exists juror_mod.t_contact_preference;
CREATE TABLE juror_mod.t_contact_preference (
	id int NOT NULL,
	contact_type varchar(5) NULL,
	description varchar(5) NULL,
	CONSTRAINT t_contact_preference_pk PRIMARY KEY (id)
);

-- insert standing data in to the juror_mod.t_contact_preference table

INSERT INTO juror_mod.t_contact_preference (id, contact_type, description) VALUES(0, 'None', 'None');
INSERT INTO juror_mod.t_contact_preference (id, contact_type, description) VALUES(1, 'Email', 'Email');
INSERT INTO juror_mod.t_contact_preference (id, contact_type, description) VALUES(2, 'Text', 'Txt');


alter table juror_mod.juror
	add constraint t_contact_preference_fk foreign key (contact_preference) references juror_mod.t_contact_preference(id);

-- create the juror_mod.expenses_rate table
drop table if exists juror_mod.expense_rate;
CREATE TABLE juror_mod.expense_rate (
	expense_type varchar(30) NOT NULL,
	rate float4 NULL,
	CONSTRAINT expense_rate_pkey PRIMARY KEY (expense_type)
);

-- insert standing data in to the juror_mod.expenses_rate table

INSERT INTO juror_mod.expense_rate (expense_type, rate) VALUES('TRAVEL_BICYCLE_PER_MILE', 0.096);
INSERT INTO juror_mod.expense_rate (expense_type, rate) VALUES('TRAVEL_MOTORCYCLE_PER_MILE', 0.314);
INSERT INTO juror_mod.expense_rate (expense_type, rate) VALUES('TRAVEL_CAR_PER_MILE', 0.314);
INSERT INTO juror_mod.expense_rate (expense_type, rate) VALUES('SUBSISTENCE_PER_DAY', 5.71);
INSERT INTO juror_mod.expense_rate (expense_type, rate) VALUES('EARNING_TEN_DAYS_FOUR_HRS_MORE', 64.95);
INSERT INTO juror_mod.expense_rate (expense_type, rate) VALUES('EARNING_TEN_DAYS_FOUR_HRS_LESS', 32.47);


-- create the juror_mod.t_id_check table
drop table if exists juror_mod.t_id_check;
CREATE TABLE juror_mod.t_id_check (
	id_check varchar(1) NOT NULL,
	description varchar(20) NOT NULL,
	CONSTRAINT t_id_check_pkey PRIMARY KEY (id_check)
);

-- insert standing data in to the juror_mod.t_id_check table

INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('A', 'Bank Statement');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('B', 'Birth Certificate');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('C', 'Credit Card');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('D', 'Drivers Licence');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('E', 'EU Nat ID Card');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('F', 'Bus Pass');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('H', 'Home Office Doc');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('I', 'Company ID');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('L', 'Cheque Bk, Crd 3Stts');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('M', 'Medical Card');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('N', 'None');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('O', 'Other');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('P', 'Passport');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('S', 'Nat Insurance Card');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('T', 'Travel Card');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('U', 'Utility Bill');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('V', 'Bank or Visa card');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('W', 'Work Permit');
INSERT INTO juror_mod.t_id_check (id_check, description) VALUES('X', 'DSS ID');

alter table juror_mod.juror_pool add constraint juror_pool_t_id_check_fk foreign key (id_checked)
	references juror_mod.t_id_check(id_check);

