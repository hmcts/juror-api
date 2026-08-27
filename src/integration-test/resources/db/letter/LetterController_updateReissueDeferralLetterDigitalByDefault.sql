UPDATE juror_mod.juror
SET dbd_preference = 'Digital'
WHERE juror_number = '555555565';

UPDATE juror_mod.bulk_print_data
SET communication_channel = 'EMAIL',
    email_status = 'PENDING',
    extracted_flag = true,
    digital_comms = true
WHERE juror_no = '555555565'
  AND form_type = '5229A';
