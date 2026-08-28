UPDATE juror_mod.notify_template_field
SET template_field = 'COURTADDRESS',
    mapper_object = 'COURT_LOC_ADDRESS'
WHERE id = 341
  AND template_id = '8a71632e-34a2-421e-9f17-100a419be7ba'
  AND template_field = 'COURTNAME'
  AND mapper_object = 'COURT_LOC_COURT_NAME';

UPDATE juror_mod.notify_template_field
SET template_field = 'email address'
WHERE id IN (346, 353, 360, 364, 372, 380, 384)
  AND template_field = 'EMAILADDRESS'
  AND mapper_object = 'JUROR_EMAIL';
