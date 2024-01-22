-- need to create the court location table before the view

select * into juror_mod.court_location from juror_digital_user.court_location;
alter table juror_mod.court_location
add constraint court_location_pkey primary key (loc_code);