update juror_mod.notify_template_field
set mapper_object = 'TEMPORARY_COURT_JURY_OFFICER_PHONE'
where template_id = 'b6915247-ff69-4740-a4b7-22505be25ef4'
and template_field = 'COURTPHONE';
