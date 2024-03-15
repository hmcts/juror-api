update juror_mod.court_location
set assembly_room = null
where loc_code in ('001', '002', '003', '004', '005');

DELETE
FROM juror_mod.courtroom
WHERE courtroom.id >= 999991;

DELETE
FROM juror_mod.welsh_court_location
WHERE loc_code in ('001', '002', '003', '004', '005');

DELETE FROM juror_mod.court_location
WHERE loc_code in ('001', '002', '003', '004', '005');