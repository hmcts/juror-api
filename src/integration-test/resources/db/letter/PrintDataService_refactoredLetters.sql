-- Synthetic values shared with the fixed-width unit test contract. Tests roll these updates back.
UPDATE juror_mod.juror
SET title = 'MR', first_name = 'FNAMEEIGHTTHREEONE', last_name = 'LNAMEEIGHTTHREEONEFIVE',
    address_line_1 = '831 STREET NAME', address_line_2 = 'ANYTOWN', address_line_3 = 'JUROR_ADDRESS_3',
    address_line_4 = 'JUROR_ADDRESS_4', address_line_5 = 'JUROR_ADDRESS_5', postcode = 'SY2 6LU'
WHERE juror_number IN ('555555561', '555555562');

UPDATE juror_mod.juror_pool SET next_date = DATE '2017-02-06'
WHERE juror_number IN ('555555561', '555555562');

UPDATE juror_mod.court_location
SET loc_court_name = 'THE CROWN COURT AT SWANSEA', loc_attend_time = TIME '10:00',
    loc_address1 = 'THE LAW COURTS', loc_address2 = 'ST HELENS ROAD', loc_address3 = 'SWANSEA',
    loc_address4 = 'SHREWSBURY', loc_address5 = 'COURT_ADDRESS_5', loc_address6 = 'COURT_ADDRESS_6',
    loc_zip = 'SY2 6LU', loc_phone = '01792637000', loc_signature = 'COURT SIGNATORY'
WHERE loc_code IN ('415', '774');

UPDATE juror_mod.court_location
SET loc_name = 'JURY CENTRAL SUMMONING BUREAU', loc_address1 = 'THE COURT SERVICE',
    loc_address2 = 'FREEPOST LON 19669', loc_address3 = 'POCOCK STREET', loc_address4 = 'LONDON',
    loc_address5 = 'BUREAU_ADDRESS_5', loc_address6 = 'BUREAU_ADDRESS_6', loc_zip = 'SE1 0YG',
    loc_phone = '0845 3555567', loc_signature = 'BUREAU SIGNATORY'
WHERE loc_code = '400';

DELETE FROM juror_mod.welsh_court_location WHERE loc_code = '415';

UPDATE juror_mod.welsh_court_location
SET loc_name = 'ABERTAWE', correspondence_name = 'YN ABERTAWE',
    loc_address1 = 'Y LLYSOEDD BARN', loc_address2 = 'LON SAN HELEN', loc_address3 = 'ABERTAWE',
    loc_address4 = 'WELSH_COURT_ADDRESS_4', loc_address5 = 'WELSH_COURT_ADDRESS_5',
    loc_address6 = 'WELSH_COURT_ADDRESS_6'
WHERE loc_code = '774';
