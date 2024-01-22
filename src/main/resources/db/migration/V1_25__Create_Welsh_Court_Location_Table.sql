-- need to create the welsh court location table before the view
select * into juror_mod.welsh_court_location from juror_digital_user.welsh_court_location;
alter table juror_mod.welsh_court_location
add constraint welsh_court_location_pkey primary key (loc_code);