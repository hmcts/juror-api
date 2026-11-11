-- Update the court location address for Cirencester address
UPDATE juror_mod.court_location
SET
	loc_address1 = 'Cirencester Court House',
	loc_address2 = 'North Way',
	loc_address3 = 'Cirencester',
	loc_address4 = 'Gloucestershire',
	loc_zip      = 'GL7 2PG'
WHERE loc_code = '795';
