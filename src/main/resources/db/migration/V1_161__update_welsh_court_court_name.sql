alter table juror_mod.welsh_court_location
    add column correspondence_name varchar(40);

update juror_mod.welsh_court_location
    set correspondence_name = loc_name;

update juror_mod.welsh_court_location
set loc_name = 'CAERDYDD'
where loc_code='411';
update juror_mod.welsh_court_location
set loc_name = 'MERTHYR TUDFUL'
where loc_code='437';
update juror_mod.welsh_court_location
set loc_name = 'CASNEWYDD DE CYMRU'
where loc_code='441';
update juror_mod.welsh_court_location
set loc_name = 'ABERTAWE'
where loc_code='457';
update juror_mod.welsh_court_location
set loc_name = 'CAERNARFON'
where loc_code='755';
update juror_mod.welsh_court_location
set loc_name = 'CAERFYRDDIN'
where loc_code='756';
update juror_mod.welsh_court_location
set loc_name = 'DOLGELLAU'
where loc_code='758';
update juror_mod.welsh_court_location
set loc_name = 'HWLFFORDD'
where loc_code='761';
update juror_mod.welsh_court_location
set loc_name = 'YR WYDDGRUG'
where loc_code='769';
update juror_mod.welsh_court_location
set loc_name = 'Y TRALLWNG'
where loc_code='774';






