ALTER TABLE juror_mod.notify_template_field
ALTER COLUMN convert_to_date TYPE boolean USING convert_to_date::boolean;
