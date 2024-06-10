alter table juror_mod.notify_template_field
    add column mapper_object varchar(255);

update juror_mod.notify_template_field
set mapper_object = 'JUROR_EMAIL'
where jd_class_name = 'juror'
  and jd_class_property = 'email';
update juror_mod.notify_template_field
set mapper_object = 'JUROR_NUMBER'
where jd_class_name = 'juror'
  and jd_class_property = 'jurorNumber';
update juror_mod.notify_template_field
set mapper_object = 'JUROR_POOL_NEXT_DATE'
where jd_class_name = 'juror'
  and jd_class_property = 'nextDate';
update juror_mod.notify_template_field
set mapper_object = 'JUROR_FIRST_NAME'
where jd_class_name = 'juror'
  and jd_class_property = 'firstName';
update juror_mod.notify_template_field
set mapper_object = 'JUROR_LAST_NAME'
where jd_class_name = 'juror'
  and jd_class_property = 'lastName';
update juror_mod.notify_template_field
set mapper_object = 'COURT_LOC_ADDRESS'
where jd_class_name = 'court'
  and jd_class_property = 'locationAddress';
update juror_mod.notify_template_field
set mapper_object = 'POOL_ATTEND_TIME'
where jd_class_name = 'pool'
  and jd_class_property = 'attendTime';
update juror_mod.notify_template_field
set mapper_object = 'COURT_JURY_OFFICER_PHONE'
where jd_class_name = 'court'
  and jd_class_property = 'juryOfficerPhone';
update juror_mod.notify_template_field
set mapper_object = 'RESPONSE_EMAIL'
where jd_class_name = 'digitalResponse'
  and jd_class_property = 'email';
update juror_mod.notify_template_field
set mapper_object = 'RESPONSE_JUROR_NUMBER'
where jd_class_name = 'digitalResponse'
  and jd_class_property = 'jurorNumber';
update juror_mod.notify_template_field
set mapper_object = 'RESPONSE_FIRST_NAME'
where jd_class_name = 'digitalResponse'
  and jd_class_property = 'firstName';
update juror_mod.notify_template_field
set mapper_object = 'RESPONSE_LAST_NAME'
where jd_class_name = 'digitalResponse'
  and jd_class_property = 'lastName';
update juror_mod.notify_template_field
set mapper_object = 'JUROR_POOL_LOC_CODE'
where jd_class_name = 'juror'
  and jd_class_property = 'court';
update juror_mod.notify_template_field
set mapper_object = 'JUROR_ALT_PHONE_NUMBER'
where jd_class_name = 'juror'
  and jd_class_property = 'altPhoneNumber';
update juror_mod.notify_template_field
set mapper_object = 'COURT_LOC_COURT_NAME'
where jd_class_name = 'court'
  and jd_class_property = 'locCourtName';
update juror_mod.notify_template_field
set mapper_object = 'RESPONSE_PHONE_NUMBER'
where jd_class_name = 'digitalResponse'
  and jd_class_property = 'phoneNumber';
update juror_mod.notify_template_field
set mapper_object = 'COURT_ADDRESS_1'
where jd_class_name = 'court'
  and jd_class_property = 'address1';
update juror_mod.notify_template_field
set mapper_object = 'COURT_ADDRESS_2'
where jd_class_name = 'court'
  and jd_class_property = 'address2';
update juror_mod.notify_template_field
set mapper_object = 'COURT_ADDRESS_3'
where jd_class_name = 'court'
  and jd_class_property = 'address3';
update juror_mod.notify_template_field
set mapper_object = 'COURT_ADDRESS_4'
where jd_class_name = 'court'
  and jd_class_property = 'address4';
update juror_mod.notify_template_field
set mapper_object = 'COURT_ADDRESS_5'
where jd_class_name = 'court'
  and jd_class_property = 'address5';
update juror_mod.notify_template_field
set mapper_object = 'COURT_LOC_POSTCODE'
where jd_class_name = 'court'
  and jd_class_property = 'postcode';


update juror_mod.notify_template_field
set mapper_object = 'BULK_PRINT_DATA'
where database_field = 'bulk_print_data.detail_rec';

alter table juror_mod.notify_template_field
    drop column jd_class_name,
    drop column jd_class_property;