UPDATE juror_mod.notify_template_mapping
SET notify_name = 'DBD_Excusal denied (English)'
WHERE template_id = '5d902f77-7505-487b-add3-b2f4ef0ed87e'
  AND template_name = 'DBD_EXC_DENIED_ENG';

UPDATE juror_mod.notify_template_mapping
SET notify_name = 'DBD_Excusal refused (Welsh)'
WHERE template_id = 'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74'
  AND template_name = 'DBD_EXC_DENIED_CY';

UPDATE juror_mod.notify_template_field
SET template_id = '1827315e-9557-4985-ba53-395cb18f76dd'
WHERE id BETWEEN 354 AND 360
  AND template_id IN (
                      '84066c25-e120-4a9c-a5e4-cdcc4c3c321d',
                      '1827315e-9557-4985-ba53-395cb18f76dd'
);

UPDATE juror_mod.notify_template_field
SET template_id = '5d902f77-7505-487b-add3-b2f4ef0ed87e'
WHERE id BETWEEN 366 AND 372
  AND template_id IN (
                      'c3b9f6e6-2d08-454f-b1e9-e3db5d95cf74',
                      '5d902f77-7505-487b-add3-b2f4ef0ed87e'
);

UPDATE juror_mod.notify_template_field
SET mapper_object = 'JUROR_POOL_SERVICE_START_DATE'
WHERE id = 351
  AND template_id = 'b3c0bb53-7f54-4fd8-af6d-25c298d59ba6'
  AND template_field = 'SERVICESTARTDATE'
  AND mapper_object = 'JUROR_POOL_DEF_DATE';

UPDATE juror_mod.notify_template_field
SET mapper_object = 'JUROR_POOL_SERVICE_START_DATE'
WHERE id IN (358, 378)
  AND template_id IN (
                      '1827315e-9557-4985-ba53-395cb18f76dd',
                      'bc749cd4-106d-4189-93c7-cac898bc2316'
)
  AND template_field = 'SERVICESTARTDATE'
  AND mapper_object = 'JUROR_POOL_NEXT_DATE';

UPDATE juror_mod.notify_template_field
SET mapper_object = 'JUROR_OR_RESPONSE_EMAIL'
WHERE id IN (372, 380, 384)
  AND template_id IN (
                      '5d902f77-7505-487b-add3-b2f4ef0ed87e',
                      'bc749cd4-106d-4189-93c7-cac898bc2316',
                      '82b30231-1980-454a-b812-0e7953f7cccf'
)
  AND template_field = 'email address'
  AND mapper_object = 'JUROR_EMAIL';
