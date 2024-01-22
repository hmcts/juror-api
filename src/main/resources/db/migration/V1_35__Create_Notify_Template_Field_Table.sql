-- procedure for creating new notify_template_field table based on the existing table whilst it still exists.

select * into juror_mod.notify_template_field from juror_digital.notify_template_field;


