---- Do not truncate standing data tables in JUROR.
-- Order is important to avoid issues with foreign key constraints!
DELETE FROM juror_mod.appearance;
DELETE FROM juror_mod.appearance_audit;
DELETE FROM JUROR_MOD.financial_audit_details_appearances;
DELETE FROM JUROR_MOD.financial_audit_details;
DELETE FROM JUROR_MOD.user_roles;
DELETE FROM JUROR_MOD.user_courts;
DELETE FROM JUROR_MOD.users;
DELETE FROM juror_mod.app_setting;
DELETE FROM juror_mod.notify_template_field;
DELETE FROM juror_mod.notify_template_mapping;
DELETE FROM juror_mod.expense_rates_public;
-- Reset the sequences as part of the begin step.  See README.md database setup for details.
