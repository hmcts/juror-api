-- Amend court name for Chichester

update juror_mod.court_location
set loc_name = 'GUILDFORD SITTING AT CHICHESTER',
loc_court_name = 'GUILDFORD SITTING AT CHICHESTER',
location_address = 'Guildford Crown Court sitting at Chichester, The Courthouse, Southgate, West Sussex, PO19 1SX'
where loc_code = '416';
