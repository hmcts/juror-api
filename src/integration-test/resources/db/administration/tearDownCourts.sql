update juror_mod.court_location
set assembly_room = null
where loc_code in ('001','002');

DELETE FROM juror_mod.courtroom
WHERE courtroom.id in (999991,999992,999993,999994);

DELETE FROM juror_mod.welsh_court_location
WHERE loc_code in ('001', '002');

DELETE FROM juror_mod.court_location
WHERE loc_code in ('001', '002', '003', '004', '005');